package io.github.thbldprncee.cardealer.storage.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;

import java.util.Map;
import java.util.UUID;

public record BuildCarConfigurationRequest(
        @NotNull
        UUID carModelId,
        @NotEmpty
        Map<ComponentType, UUID> selectedComponentOptionIds
) {
}
