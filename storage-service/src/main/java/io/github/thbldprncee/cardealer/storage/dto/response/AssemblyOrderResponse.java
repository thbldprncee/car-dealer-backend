package io.github.thbldprncee.cardealer.storage.dto.response;

import io.github.thbldprncee.cardealer.storage.domain.enums.AssemblyOrderStatus;
import io.github.thbldprncee.cardealer.events.OrderType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AssemblyOrderResponse(
        UUID id,
        UUID sourceOrderId,
        OrderType sourceOrderType,
        UUID carId,
        UUID carModelId,
        Set<UUID> requiredComponentOptionIds,
        UUID warehouseAdminId,
        AssemblyOrderStatus status,
        String failureReason,
        String traceId,
        Instant createdAt,
        Instant updatedAt
) {
}
