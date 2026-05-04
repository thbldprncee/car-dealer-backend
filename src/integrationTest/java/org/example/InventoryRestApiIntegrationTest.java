package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryRestApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldGetSeedCarModels() throws Exception {
        mockMvc.perform(get("/api/inventory/car-models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].brand").exists());
    }

    @Test
    void shouldGetCarModelById() throws Exception {
        mockMvc.perform(get("/api/inventory/car-models/{id}", BMW_320I_MODEL_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BMW_320I_MODEL_ID.toString()))
                .andExpect(jsonPath("$.brand").value("BMW"))
                .andExpect(jsonPath("$.modelName").value("320i"));
    }

    @Test
    void shouldFilterCarModelsByBrandAndComponentOptions() throws Exception {
        String body = json(Map.of(
                "brand", "BMW",
                "componentOptionIds", Set.of(WHEELS_STANDARD_ID.toString())
        ));

        mockMvc.perform(post("/api/inventory/car-models/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].brand").value("BMW"));
    }

    @Test
    void shouldCreateGetUpdateAndDeleteInventoryCar() throws Exception {
        String vin = "VIN-INV-IT-" + UUID.randomUUID();

        String createBody = json(Map.of(
                "carModelId", BMW_320I_MODEL_ID.toString(),
                "vin", vin,
                "color", "BLACK",
                "price", new BigDecimal("4100000"),
                "available", true,
                "availableForTestDrive", true
        ));

        String response = mockMvc.perform(post("/api/inventory/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin").value(vin))
                .andExpect(jsonPath("$.color").value("BLACK"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID carId = UUID.fromString(map(response).get("id").toString());

        mockMvc.perform(get("/api/inventory/cars/{id}", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId.toString()))
                .andExpect(jsonPath("$.vin").value(vin));

        String updateBody = json(Map.of(
                "color", "WHITE",
                "price", new BigDecimal("4200000"),
                "available", true,
                "availableForTestDrive", false
        ));

        mockMvc.perform(put("/api/inventory/cars/{id}", carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("WHITE"))
                .andExpect(jsonPath("$.availableForTestDrive").value(false));

        mockMvc.perform(delete("/api/inventory/cars/{id}", carId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldCreateGetAndDeleteComponentOption() throws Exception {
        String name = "IT wheels " + UUID.randomUUID();

        String createBody = json(Map.of(
                "componentType", "WHEELS",
                "name", name,
                "priceDelta", new BigDecimal("12345"),
                "compatibleCarModelIds", Set.of(BMW_320I_MODEL_ID.toString())
        ));

        String response = mockMvc.perform(post("/api/inventory/component-options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.componentType").value("WHEELS"))
                .andExpect(jsonPath("$.name").value(name))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID optionId = UUID.fromString(map(response).get("id").toString());

        mockMvc.perform(get("/api/inventory/component-options/{id}", optionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(optionId.toString()));

        mockMvc.perform(get("/api/inventory/component-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(delete("/api/inventory/component-options/{id}", optionId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldCreateGetUpdateAndDeleteSparePart() throws Exception {
        String name = "IT spare part " + UUID.randomUUID();

        String createBody = json(Map.of(
                "name", name,
                "description", "Created from integration test",
                "price", new BigDecimal("7777"),
                "compatibleCarModelIds", Set.of(BMW_320I_MODEL_ID.toString())
        ));

        String response = mockMvc.perform(post("/api/inventory/spare-parts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID sparePartId = UUID.fromString(map(response).get("id").toString());

        mockMvc.perform(get("/api/inventory/spare-parts/{id}", sparePartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sparePartId.toString()));

        String updateBody = json(Map.of(
                "name", name + " updated",
                "description", "Updated from integration test",
                "price", new BigDecimal("8888"),
                "compatibleCarModelIds", Set.of(BMW_320I_MODEL_ID.toString())
        ));

        mockMvc.perform(put("/api/inventory/spare-parts/{id}", sparePartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name + " updated"))
                .andExpect(jsonPath("$.price").value(8888));

        mockMvc.perform(delete("/api/inventory/spare-parts/{id}", sparePartId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetAllCarsAndSpareParts() throws Exception {
        mockMvc.perform(get("/api/inventory/cars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(get("/api/inventory/spare-parts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }
}
