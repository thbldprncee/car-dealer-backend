package org.example.dto.request;

import org.example.domain.enums.CarColor;

import java.math.BigDecimal;

public record UpdateCarRequest(
        CarColor color,
        BigDecimal price,
        Boolean available,
        Boolean availableForTestDrive
) {
}