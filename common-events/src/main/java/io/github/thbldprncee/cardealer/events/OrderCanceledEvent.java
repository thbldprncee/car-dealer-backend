package io.github.thbldprncee.cardealer.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Compensation step of the approval saga: the order was canceled after it had been sent to the
 * warehouse, so whatever the warehouse reserved for it must be released.
 */
public record OrderCanceledEvent(
        UUID eventId,
        UUID orderId,
        OrderType orderType,
        UUID carId,
        String traceId,
        Instant occurredAt
) {
}
