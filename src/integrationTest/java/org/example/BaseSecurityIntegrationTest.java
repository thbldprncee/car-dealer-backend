package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseSecurityIntegrationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("car_dealer_test")
            .withUsername("postgres")
            .withPassword("postgres");

    static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:26.0")
            .withRealmImportFile("keycloak/car-dealer-realm.json");

    static {
        Startables.deepStart(POSTGRES, KEYCLOAK).join();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    private TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> KEYCLOAK.getAuthServerUrl() + "/realms/car-dealer");
    }

    @BeforeAll
    void setUpRestTemplate() {
        this.restTemplate = new TestRestTemplate();
    }

    protected String getToken(String username, String password) throws Exception {
        String tokenUrl = KEYCLOAK.getAuthServerUrl()
                + "/realms/car-dealer/protocol/openid-connect/token";

        org.springframework.util.LinkedMultiValueMap<String, String> form =
                new org.springframework.util.LinkedMultiValueMap<>();

        form.add("client_id", "car-dealer-backend");
        form.add("username", username);
        form.add("password", password);
        form.add("grant_type", "password");

        String response = restTemplate.postForObject(
                tokenUrl,
                form,
                String.class
        );

        JsonNode json = OBJECT_MAPPER.readTree(response);

        if (json.has("error")) {
            throw new IllegalStateException("Cannot get token from Keycloak: " + response);
        }

        return json.get("access_token").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}