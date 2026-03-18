package org.example.dto.request;

import java.util.UUID;

public record CreateCustomCarOrderRequest(
        UUID clientId,
        UUID carModelId,
        UUID carConfigurationId
) {
}