package org.example.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AvailableCarResponse(
        UUID id,
        UUID carModelId,
        String carModelDisplayName,
        String vin,
        String color,
        BigDecimal price,
        boolean available,
        boolean availableForTestDrive
) {
}
