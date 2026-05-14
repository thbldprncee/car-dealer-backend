package org.example.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderSentForApprovalEvent(
        UUID eventId,
        UUID orderId,
        OrderType orderType,
        UUID carId,
        UUID carModelId,
        UUID configurationId,
        List<UUID> componentOptionIds,
        String traceId,
        Instant occurredAt
) {
}
