package io.github.thbldprncee.cardealer.order.domain.enums;

public enum InStockOrderStatus {
    CREATED,
    APPROVED_BY_MANAGER,
    WAITING_FOR_PAYMENT,
    PAID,
    READY_FOR_DELIVERY,
    COMPLETED,
    CANCELED
}