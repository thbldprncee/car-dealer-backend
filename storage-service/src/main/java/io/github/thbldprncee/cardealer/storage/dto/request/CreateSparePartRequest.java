package io.github.thbldprncee.cardealer.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateSparePartRequest(
        @NotBlank
        String name,
        @NotBlank
        String description,
        @NotNull
        @PositiveOrZero
        BigDecimal price,
        @NotEmpty
        Set<UUID> compatibleCarModelIds
) {
}
