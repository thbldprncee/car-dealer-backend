package org.example.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.example.domain.enums.ComponentType;

import java.util.Map;
import java.util.UUID;

public record BuildCarConfigurationRequest(
        @NotNull
        UUID carModelId,
        @NotEmpty
        Map<ComponentType, UUID> selectedComponentOptionIds
) {
}
