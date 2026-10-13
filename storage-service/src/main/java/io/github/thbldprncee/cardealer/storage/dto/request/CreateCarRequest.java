package io.github.thbldprncee.cardealer.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import io.github.thbldprncee.cardealer.storage.domain.enums.CarColor;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCarRequest(
        @NotNull
        UUID carModelId,
        @NotBlank
        String vin,
        @NotNull
        CarColor color,
        @NotNull
        @PositiveOrZero
        BigDecimal price,
        boolean available,
        boolean availableForTestDrive
) {
}
