package org.example.dto.response;

import org.example.domain.enums.AssemblyOrderStatus;
import org.example.events.OrderType;

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
