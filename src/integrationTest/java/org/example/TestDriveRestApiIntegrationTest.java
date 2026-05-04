package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TestDriveRestApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldGetCarsAvailableForTestDrive() throws Exception {
        mockMvc.perform(get("/api/test-drives/cars"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateGetApproveCompleteAndDeleteTestDriveRequest() throws Exception {
        UUID carId = createCarForTestDrive();

        String body = json(Map.of(
                "clientId", CLIENT_ID.toString(),
                "carId", carId.toString(),
                "startDateTime", LocalDateTime.now().plusDays(2).withNano(0).toString()
        ));

        String response = mockMvc.perform(post("/api/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID.toString()))
                .andExpect(jsonPath("$.carId").value(carId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID requestId = UUID.fromString(map(response).get("id").toString());

        mockMvc.perform(get("/api/test-drives/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId.toString()));

        mockMvc.perform(get("/api/test-drives"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));

        mockMvc.perform(get("/api/test-drives?status=CREATED"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/test-drives/{id}/approve", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(patch("/api/test-drives/{id}/complete", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(delete("/api/test-drives/{id}", requestId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldCreateAndCancelTestDriveRequest() throws Exception {
        UUID carId = createCarForTestDrive();

        String body = json(Map.of(
                "clientId", CLIENT_ID.toString(),
                "carId", carId.toString(),
                "startDateTime", LocalDateTime.now().plusDays(3).withNano(0).toString()
        ));

        String response = mockMvc.perform(post("/api/test-drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID requestId = UUID.fromString(map(response).get("id").toString());

        mockMvc.perform(patch("/api/test-drives/{id}/cancel", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldAddAndRemoveCarFromTestDriveList() throws Exception {
        UUID carId = createCarNotAvailableForTestDrive();

        mockMvc.perform(patch("/api/test-drives/cars/{carId}/add", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableForTestDrive").value(true));

        mockMvc.perform(patch("/api/test-drives/cars/{carId}/remove", carId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableForTestDrive").value(false));
    }

    private UUID createCarForTestDrive() throws Exception {
        String vin = "VIN-TD-IT-" + UUID.randomUUID();

        String body = json(Map.of(
                "carModelId", BMW_320I_MODEL_ID.toString(),
                "vin", vin,
                "color", "WHITE",
                "price", new BigDecimal("4600000"),
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

    private UUID createCarNotAvailableForTestDrive() throws Exception {
        String vin = "VIN-TD-NO-IT-" + UUID.randomUUID();

        String body = json(Map.of(
                "carModelId", BMW_320I_MODEL_ID.toString(),
                "vin", vin,
                "color", "BLACK",
                "price", new BigDecimal("4700000"),
                "available", true,
                "availableForTestDrive", false
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
}
