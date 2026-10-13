package io.github.thbldprncee.cardealer.order.domain.model;

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
import io.github.thbldprncee.cardealer.order.domain.enums.CustomOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Getter
@Entity
@Table(name = "custom_car_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class CustomCarOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    @ToString.Exclude
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_manager_id", nullable = false)
    @ToString.Exclude
    private SalesManager salesManager;

    @Column(name = "car_model_id", nullable = false)
    private UUID carModelId;

    @Column(name = "car_model_display_name", nullable = false)
    private String carModelDisplayName;

    @Column(name = "car_configuration_id", nullable = false)
    private UUID carConfigurationId;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomOrderStatus status;

    public CustomCarOrder(
            UUID id,
            Client client,
            SalesManager salesManager,
            UUID carModelId,
            String carModelDisplayName,
            UUID carConfigurationId,
            BigDecimal totalPrice,
            CustomOrderStatus status
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Custom car order id must not be null"));
        this.client = Optional.ofNullable(client)
                .orElseThrow(() -> new DomainValidationException("Client must not be null"));
        this.salesManager = Optional.ofNullable(salesManager)
                .orElseThrow(() -> new DomainValidationException("Sales manager must not be null"));
        this.carModelId = Optional.ofNullable(carModelId)
                .orElseThrow(() -> new DomainValidationException("Car model id must not be null"));
        this.carModelDisplayName = validateRequiredText(carModelDisplayName, "Car model display name must not be blank");
        this.carConfigurationId = Optional.ofNullable(carConfigurationId)
                .orElseThrow(() -> new DomainValidationException("Car configuration id must not be null"));
        this.totalPrice = Optional.ofNullable(totalPrice)
                .filter(price -> price.compareTo(BigDecimal.ZERO) >= 0)
                .orElseThrow(() -> new DomainValidationException("Total price must not be negative"));
        this.status = Optional.ofNullable(status)
                .orElseThrow(() -> new DomainValidationException("Custom car order status must not be null"));
    }

    public void approveByWarehouseAfterApprovalResult() {
        if (status != CustomOrderStatus.CREATED) {
            return;
        }
        this.status = CustomOrderStatus.APPROVED_BY_WAREHOUSE;
    }

    public void markWaitingForPayment() {
        ensureStatus(
                CustomOrderStatus.APPROVED_BY_WAREHOUSE,
                "Only an approved custom car order can be moved to waiting for payment"
        );
        this.status = CustomOrderStatus.WAITING_FOR_PAYMENT;
    }

    public void markAsPaid() {
        ensureStatus(
                CustomOrderStatus.WAITING_FOR_PAYMENT,
                "Only a custom car order waiting for payment can be paid"
        );
        this.status = CustomOrderStatus.PAID;
    }

    public void markWaitingForDelivery() {
        ensureStatus(
                CustomOrderStatus.PAID,
                "Only a paid custom car order can be moved to waiting for delivery"
        );
        this.status = CustomOrderStatus.WAITING_FOR_DELIVERY;
    }

    public void markReadyForDelivery() {
        ensureStatus(
                CustomOrderStatus.WAITING_FOR_DELIVERY,
                "Only a custom car order waiting for delivery can be marked as ready for delivery"
        );
        this.status = CustomOrderStatus.READY_FOR_DELIVERY;
    }
    /** Rejection can only arrive while the order still waits for the warehouse check. */
    public void cancelAfterWarehouseRejection(String reason) {
        if (status == CustomOrderStatus.CANCELED) {
            return;
        }
        ensureStatus(
                CustomOrderStatus.CREATED,
                "Only a custom order awaiting warehouse approval can be rejected by the warehouse"
        );
        this.status = CustomOrderStatus.CANCELED;
    }

    public void complete() {
        ensureStatus(
                CustomOrderStatus.READY_FOR_DELIVERY,
                "Only a custom car order ready for delivery can be completed"
        );
        this.status = CustomOrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == CustomOrderStatus.COMPLETED) {
            throw new DomainValidationException("Completed custom car order cannot be canceled");
        }

        if (status == CustomOrderStatus.CANCELED) {
            throw new DomainValidationException("Custom car order is already canceled");
        }

        this.status = CustomOrderStatus.CANCELED;
    }

    private void ensureStatus(CustomOrderStatus expected, String message) {
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
