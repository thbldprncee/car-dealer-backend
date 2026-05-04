package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LiquibaseMigrationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldApplyLiquibaseMigrationsAndCreateTables() {
        Integer tableCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'public'
                          and table_name in (
                              'users',
                              'component_options',
                              'car_models',
                              'cars',
                              'spare_parts',
                              'car_configurations',
                              'in_stock_car_orders',
                              'custom_car_orders',
                              'test_drive_requests'
                          )
                        """,
                Integer.class
        );

        assertTrue(tableCount != null && tableCount >= 9);
    }

    @Test
    void shouldApplySeedData() {
        Integer carModelCount = jdbcTemplate.queryForObject(
                "select count(*) from car_models where removed = false",
                Integer.class
        );

        Integer carCount = jdbcTemplate.queryForObject(
                "select count(*) from cars where removed = false",
                Integer.class
        );

        assertTrue(carModelCount != null && carModelCount > 0);
        assertTrue(carCount != null && carCount > 0);
    }
}