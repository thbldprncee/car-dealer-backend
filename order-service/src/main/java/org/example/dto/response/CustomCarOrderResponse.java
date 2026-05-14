package org.example.dto.response;

import org.example.domain.enums.CustomOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomCarOrderResponse(
        UUID id,
        UUID clientId,
        String clientFullName,
        UUID salesManagerId,
        String salesManagerFullName,
        UUID carModelId,
        String carModelDisplayName,
        UUID carConfigurationId,
        BigDecimal totalPrice,
        CustomOrderStatus status,
        Instant createdAt
) {
}
