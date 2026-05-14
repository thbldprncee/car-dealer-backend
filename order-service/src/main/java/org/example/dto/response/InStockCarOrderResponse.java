package org.example.dto.response;

import org.example.domain.enums.InStockOrderStatus;

import java.time.Instant;
import java.util.UUID;

public record InStockCarOrderResponse(
        UUID id,
        UUID clientId,
        String clientFullName,
        UUID salesManagerId,
        String salesManagerFullName,
        UUID carId,
        String carDisplayName,
        InStockOrderStatus status,
        Instant createdAt
) {
}
