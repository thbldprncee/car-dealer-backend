package io.github.thbldprncee.cardealer.storage.dto.response;

import io.github.thbldprncee.cardealer.storage.domain.enums.CarColor;

import java.math.BigDecimal;
import java.util.UUID;

public record CarResponse(
        UUID id,
        UUID carModelId,
        String carModelDisplayName,
        String vin,
        CarColor color,
        BigDecimal price,
        boolean available,
        boolean availableForTestDrive
) {
}