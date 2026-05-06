package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIntegrationTest {

    protected static final UUID CLIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    protected static final UUID SALES_MANAGER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    protected static final UUID WAREHOUSE_ADMIN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    protected static final UUID ADMIN_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    protected static final UUID BMW_320I_MODEL_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1");

    protected static final UUID WHEELS_STANDARD_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
    protected static final UUID WHEELS_MSPORT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2");
    protected static final UUID TRANSMISSION_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3");
    protected static final UUID STEERING_STANDARD_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4");
    protected static final UUID STEERING_MSPORT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5");
    protected static final UUID INTERIOR_FABRIC_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6");
    protected static final UUID INTERIOR_LEATHER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7");

    protected static final UUID SEED_CAR_1_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");
    protected static final UUID SEED_CAR_2_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc2");

    protected static final UUID SPARE_PART_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddd01");

    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("car_dealer_test")
            .withUsername("postgres")
            .withPassword("postgres");

    protected static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
            .withRealmImportFile("keycloak/car-dealer-realm.json");

    private static final ObjectMapper STATIC_OBJECT_MAPPER = new ObjectMapper();

    static {
        POSTGRES.start();
        KEYCLOAK.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected final TestRestTemplate restTemplate = new TestRestTemplate();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KEYCLOAK.getAuthServerUrl() + "/realms/car-dealer");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> KEYCLOAK.getAuthServerUrl() + "/realms/car-dealer/protocol/openid-connect/certs");
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected Map<String, Object> map(String json) throws Exception {
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

    protected String getToken(String username, String password) throws Exception {
        String tokenUrl = KEYCLOAK.getAuthServerUrl() + "/realms/car-dealer/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=car-dealer-backend"
                + "&username=" + username
                + "&password=" + password
                + "&grant_type=password";

        String response = restTemplate.postForObject(tokenUrl, new HttpEntity<>(body, headers), String.class);

        Map<String, Object> responseMap = STATIC_OBJECT_MAPPER.readValue(response, new TypeReference<>() {});

        if (responseMap.containsKey("error")) {
            throw new IllegalStateException(response);
        }

        return responseMap.get("access_token").toString();
    }

    protected String userToken() throws Exception {
        return getToken("user", "user");
    }

    protected String managerToken() throws Exception {
        return getToken("manager", "manager");
    }

    protected String warehouseAdminToken() throws Exception {
        return getToken("warehouse", "warehouse");
    }

    protected String adminToken() throws Exception {
        return getToken("admin", "admin");
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}