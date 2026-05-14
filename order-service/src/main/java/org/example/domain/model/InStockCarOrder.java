package org.example.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.domain.enums.InStockOrderStatus;
import org.example.domain.exceptions.DomainValidationException;

import java.util.Optional;
import java.util.UUID;

@Getter
@Entity
@Table(name = "in_stock_car_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class InStockCarOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_manager_id", nullable = false)
    @ToString.Exclude
    private SalesManager salesManager;

    @Column(name = "car_id", nullable = false)
    private UUID carId;

    @Column(name = "car_display_name", nullable = false)
    private String carDisplayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InStockOrderStatus status;

    public InStockCarOrder(
            UUID id,
            Client client,
            SalesManager salesManager,
            UUID carId,
            String carDisplayName,
            InStockOrderStatus status
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("In-stock car order id must not be null"));
        this.client = Optional.ofNullable(client)
                .orElseThrow(() -> new DomainValidationException("Client must not be null"));
        this.salesManager = Optional.ofNullable(salesManager)
                .orElseThrow(() -> new DomainValidationException("Sales manager must not be null"));
        this.carId = Optional.ofNullable(carId)
                .orElseThrow(() -> new DomainValidationException("Car id must not be null"));
        this.carDisplayName = validateRequiredText(carDisplayName, "Car display name must not be blank");
        this.status = Optional.ofNullable(status)
                .orElseThrow(() -> new DomainValidationException("In-stock car order status must not be null"));
    }

    public void approveByManager() {
        ensureStatus(
                InStockOrderStatus.CREATED,
                "Only a created in-stock order can be approved by manager"
        );
        this.status = InStockOrderStatus.APPROVED_BY_MANAGER;
    }

    public void markWaitingForPayment() {
        ensureStatus(
                InStockOrderStatus.APPROVED_BY_MANAGER,
                "Only an approved in-stock order can be moved to waiting for payment"
        );
        this.status = InStockOrderStatus.WAITING_FOR_PAYMENT;
    }

    public void markAsPaid() {
        ensureStatus(
                InStockOrderStatus.WAITING_FOR_PAYMENT,
                "Only an in-stock order waiting for payment can be paid"
        );
        this.status = InStockOrderStatus.PAID;
    }

    public void markReadyForDelivery() {
        ensureStatus(
                InStockOrderStatus.PAID,
                "Only a paid in-stock order can be marked as ready for delivery"
        );
        this.status = InStockOrderStatus.READY_FOR_DELIVERY;
    }

    public void markReadyForDeliveryAfterWarehouseApproval() {
        if (status == InStockOrderStatus.READY_FOR_DELIVERY) {
            return;
        }
        if (status == InStockOrderStatus.CANCELED) {
            return;
        }
        ensureStatus(
                InStockOrderStatus.PAID,
                "Only a paid in-stock order can be marked as ready for delivery"
        );
        this.status = InStockOrderStatus.READY_FOR_DELIVERY;
    }

    public void cancelAfterWarehouseRejection(String reason) {
        if (status == InStockOrderStatus.CANCELED) {
            return;
        }
        if (status == InStockOrderStatus.READY_FOR_DELIVERY) {
            return;
        }
        ensureStatus(
                InStockOrderStatus.PAID,
                "Only a paid in-stock order can be canceled after warehouse rejection"
        );
        this.status = InStockOrderStatus.CANCELED;
    }

    public void complete() {
        ensureStatus(
                InStockOrderStatus.READY_FOR_DELIVERY,
                "Only an in-stock order ready for delivery can be completed"
        );
        this.status = InStockOrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == InStockOrderStatus.COMPLETED) {
            throw new DomainValidationException("Completed in-stock order cannot be canceled");
        }

        if (status == InStockOrderStatus.CANCELED) {
            throw new DomainValidationException("In-stock order is already canceled");
        }

        this.status = InStockOrderStatus.CANCELED;
    }

    private void ensureStatus(InStockOrderStatus expected, String message) {
        if (status != expected) {
            throw new DomainValidationException(message);
        }
    }

    private String validateRequiredText(String value, String message) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException(message));
    }
}
