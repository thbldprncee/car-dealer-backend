package org.example;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityKeycloakIntegrationTest extends BaseSecurityIntegrationTest {

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/orders/in-stock"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowRequestWithRealKeycloakManagerToken() throws Exception {
        String token = getToken("manager", "manager");

        mockMvc.perform(get("/api/orders/in-stock")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void shouldRejectInStockOrdersForClientRole() throws Exception {
        String token = getToken("user", "user");

        mockMvc.perform(get("/api/orders/in-stock")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());
    }
}