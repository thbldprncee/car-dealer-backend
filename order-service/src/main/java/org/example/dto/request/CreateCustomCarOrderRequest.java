package org.example.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCustomCarOrderRequest(
        @NotNull
        UUID carModelId,
        @NotNull
        UUID carConfigurationId
) {
}
