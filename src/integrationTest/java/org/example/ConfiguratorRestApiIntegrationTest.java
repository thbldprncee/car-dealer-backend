package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConfiguratorRestApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldBuildAndGetConfiguration() throws Exception {
        String body = json(Map.of(
                "carModelId", BMW_320I_MODEL_ID.toString(),
                "selectedComponentOptionIds", Map.of(
                        "WHEELS", WHEELS_MSPORT_ID.toString(),
                        "TRANSMISSION", TRANSMISSION_ID.toString(),
                        "STEERING_WHEEL", STEERING_MSPORT_ID.toString(),
                        "INTERIOR", INTERIOR_LEATHER_ID.toString()
                )
        ));

        String response = mockMvc.perform(post("/api/configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carModelId").value(BMW_320I_MODEL_ID.toString()))
                .andExpect(jsonPath("$.selectedComponents.WHEELS.id").value(WHEELS_MSPORT_ID.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID configurationId = UUID.fromString(map(response).get("id").toString());

        mockMvc.perform(get("/api/configurations/{id}", configurationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(configurationId.toString()))
                .andExpect(jsonPath("$.carModelId").value(BMW_320I_MODEL_ID.toString()));
    }
}
