package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderRestApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateGetListAndCancelInStockOrder() throws Exception {
        UUID carId = createCarForOrder();

        String body = json(Map.of(
                "clientId", CLIENT_ID.toString(),
                "carId", carId.toString()
        ));

        String response = mockMvc.perform(post("/api/orders/in-stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID.toString()))
                .andExpect(jsonPath("$.carId").value(carId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID orderId = UUID.fromString(map(response).get("id").toString());

        mockMvc.perform(get("/api/orders/in-stock/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));

        mockMvc.perform(get("/api/orders/in-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(patch("/api/orders/in-stock/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldCreateGetListAndCancelCustomOrder() throws Exception {
        UUID configurationId = createConfigurationForOrder();

        String body = json(Map.of(
                "clientId", CLIENT_ID.toString(),
                "carModelId", BMW_320I_MODEL_ID.toString(),
                "carConfigurationId", configurationId.toString()
        ));

        String response = mockMvc.perform(post("/api/orders/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID.toString()))
                .andExpect(jsonPath("$.carModelId").value(BMW_320I_MODEL_ID.toString()))
                .andExpect(jsonPath("$.carConfigurationId").value(configurationId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID orderId = UUID.fromString(map(response).get("id").toString());

        mockMvc.perform(get("/api/orders/custom/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));

        mockMvc.perform(get("/api/orders/custom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(patch("/api/orders/custom/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    private UUID createCarForOrder() throws Exception {
        String vin = "VIN-ORDER-IT-" + UUID.randomUUID();

        String body = json(Map.of(
                "carModelId", BMW_320I_MODEL_ID.toString(),
                "vin", vin,
                "color", "BLACK",
                "price", new BigDecimal("4500000"),
                "available", true,
                "availableForTestDrive", true
        ));

        String response = mockMvc.perform(post("/api/inventory/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(map(response).get("id").toString());
    }

    private UUID createConfigurationForOrder() throws Exception {
        String body = json(Map.of(
                "carModelId", BMW_320I_MODEL_ID.toString(),
                "selectedComponentOptionIds", Map.of(
                        "WHEELS", WHEELS_STANDARD_ID.toString(),
                        "TRANSMISSION", TRANSMISSION_ID.toString(),
                        "STEERING_WHEEL", STEERING_STANDARD_ID.toString(),
                        "INTERIOR", INTERIOR_FABRIC_ID.toString()
                )
        ));

        String response = mockMvc.perform(post("/api/configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(map(response).get("id").toString());
    }
}
