package io.github.thbldprncee.cardealer.order.dto.response;

import io.github.thbldprncee.cardealer.order.domain.enums.InStockOrderStatus;

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
