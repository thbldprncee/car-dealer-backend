package io.github.thbldprncee.cardealer.storage.dto.request;

import io.github.thbldprncee.cardealer.storage.domain.enums.CarColor;

import java.math.BigDecimal;

public record UpdateCarRequest(
        CarColor color,
        BigDecimal price,
        Boolean available,
        Boolean availableForTestDrive
) {
}