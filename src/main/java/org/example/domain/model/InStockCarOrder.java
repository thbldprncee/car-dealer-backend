package org.example.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.example.domain.enums.InStockOrderStatus;
import org.example.domain.exceptions.DomainValidationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class InStockCarOrder {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final Client client;
    private final SalesManager salesManager;
    private final Car car;
    private final LocalDateTime createdAt;

    private InStockOrderStatus status;

    public InStockCarOrder(
            UUID id,
            Client client,
            SalesManager salesManager,
            Car car,
            LocalDateTime createdAt,
            InStockOrderStatus status
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("In-stock order id must not be null"));
        this.client = Optional.ofNullable(client)
                .orElseThrow(() -> new DomainValidationException("Client must not be null"));
        this.salesManager = Optional.ofNullable(salesManager)
                .orElseThrow(() -> new DomainValidationException("Sales manager must not be null"));
        this.car = Optional.ofNullable(car)
                .orElseThrow(() -> new DomainValidationException("Car must not be null"));
        this.createdAt = Optional.ofNullable(createdAt)
                .orElseThrow(() -> new DomainValidationException("Created at must not be null"));
        this.status = Optional.ofNullable(status)
                .orElseThrow(() -> new DomainValidationException("Order status must not be null"));

        validateCarAvailability();
    }

    public void approveByManager() {
        ensureStatus(InStockOrderStatus.CREATED,
                "Only an order in CREATED status can be approved by manager");
        this.status = InStockOrderStatus.APPROVED_BY_MANAGER;
    }

    public void markWaitingForPayment() {
        ensureStatus(InStockOrderStatus.APPROVED_BY_MANAGER,
                "Only an order approved by manager can move to WAITING_FOR_PAYMENT");
        this.status = InStockOrderStatus.WAITING_FOR_PAYMENT;
    }

    public void markAsPaid() {
        ensureStatus(InStockOrderStatus.WAITING_FOR_PAYMENT,
                "Only an order in WAITING_FOR_PAYMENT status can be marked as PAID");
        this.status = InStockOrderStatus.PAID;
    }

    public void markReadyForDelivery() {
        ensureStatus(InStockOrderStatus.PAID,
                "Only a paid order can move to READY_FOR_DELIVERY");
        this.status = InStockOrderStatus.READY_FOR_DELIVERY;
    }

    public void complete() {
        ensureStatus(InStockOrderStatus.READY_FOR_DELIVERY,
                "Only an order in READY_FOR_DELIVERY status can be completed");
        this.status = InStockOrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == InStockOrderStatus.COMPLETED) {
            throw new DomainValidationException("A completed order cannot be canceled");
        }
        if (status == InStockOrderStatus.CANCELED) {
            throw new DomainValidationException("Order is already canceled");
        }
        this.status = InStockOrderStatus.CANCELED;
    }

    private void validateCarAvailability() {
        if (!car.isAvailable()) {
            throw new DomainValidationException("Car is not available for purchase");
        }
    }

    private void ensureStatus(InStockOrderStatus expected, String message) {
        if (status != expected) {
            throw new DomainValidationException(message);
        }
    }
}