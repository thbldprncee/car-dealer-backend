package org.example.events;

import java.time.Instant;
import java.util.UUID;

public record OrderApprovalResultEvent(
        UUID eventId,
        UUID orderId,
        OrderType orderType,
        ApprovalResult result,
        String reason,
        String traceId,
        Instant occurredAt
) {
}
