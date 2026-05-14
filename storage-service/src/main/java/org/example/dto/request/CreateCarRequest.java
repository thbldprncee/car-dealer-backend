package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.example.domain.enums.CarColor;

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
