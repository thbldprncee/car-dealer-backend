package org.example.dto.request;

import org.example.domain.enums.CarColor;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCarRequest(
        UUID carModelId,
        String vin,
        CarColor color,
        BigDecimal price,
        boolean available,
        boolean availableForTestDrive
) {
}