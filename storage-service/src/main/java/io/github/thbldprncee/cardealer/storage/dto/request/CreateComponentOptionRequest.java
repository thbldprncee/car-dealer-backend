package io.github.thbldprncee.cardealer.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateComponentOptionRequest(
        @NotNull
        ComponentType componentType,
        @NotBlank
        String name,
        @NotNull
        BigDecimal priceDelta,
        @NotEmpty
        Set<UUID> compatibleCarModelIds
) {
}
