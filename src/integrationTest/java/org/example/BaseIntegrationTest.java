package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("car_dealer_test")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
    }

    protected static final UUID CLIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    protected static final UUID SALES_MANAGER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    protected static final UUID WAREHOUSE_ADMIN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    protected static final UUID ADMIN_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    protected static final UUID WHEELS_STANDARD_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa1");
    protected static final UUID WHEELS_MSPORT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa2");
    protected static final UUID TRANSMISSION_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa3");
    protected static final UUID STEERING_STANDARD_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa4");
    protected static final UUID STEERING_MSPORT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa5");
    protected static final UUID INTERIOR_FABRIC_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa6");
    protected static final UUID INTERIOR_LEATHER_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaa7");

    protected static final UUID BMW_320I_MODEL_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1");

    protected static final UUID SEED_CAR_1_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc1");
    protected static final UUID SEED_CAR_2_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-ccccccccccc2");

    protected static final UUID SPARE_PART_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-ddddddddddd1");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.liquibase.enabled", () -> "true");

        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "5000");
    }

    @BeforeEach
    void configureObjectMapper() {
        objectMapper.findAndRegisterModules();
    }

    protected String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    protected Map<String, Object> map(String json) throws Exception {
        return objectMapper.readValue(json, Map.class);
    }
}
