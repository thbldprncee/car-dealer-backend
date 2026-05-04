package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CarRestApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldGetCars() throws Exception {
        mockMvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(get("/api/cars/available"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cars/test-drive"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCarById() throws Exception {
        mockMvc.perform(get("/api/cars/{id}", SEED_CAR_1_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SEED_CAR_1_ID.toString()));
    }

    @Test
    void shouldFilterCars() throws Exception {
        String body = json(Map.of(
                "brand", "BMW",
                "color", "BLACK",
                "minPrice", new BigDecimal("3000000"),
                "maxPrice", new BigDecimal("5000000")
        ));

        mockMvc.perform(post("/api/cars/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateUpdateAndDeleteCar() throws Exception {
        String vin = "VIN-CAR-IT-" + UUID.randomUUID();

        String createBody = json(Map.of(
                "carModelId", BMW_320I_MODEL_ID.toString(),
                "vin", vin,
                "color", "WHITE",
                "price", new BigDecimal("4300000"),
                "available", true,
                "availableForTestDrive", true
        ));

        String response = mockMvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin").value(vin))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID carId = UUID.fromString(map(response).get("id").toString());

        String updateBody = json(Map.of(
                "color", "BLACK",
                "price", new BigDecimal("4400000"),
                "available", false,
                "availableForTestDrive", false
        ));

        mockMvc.perform(put("/api/cars/{id}", carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("BLACK"))
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(delete("/api/cars/{id}", carId))
                .andExpect(status().isNoContent());
    }
}
