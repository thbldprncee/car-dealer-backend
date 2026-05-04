package org.example.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.domain.enums.CustomOrderStatus;
import org.example.domain.exceptions.DomainValidationException;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_model_id", nullable = false)
    @ToString.Exclude
    private CarModel carModel;

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "car_configuration_id", nullable = false, unique = true)
    @ToString.Exclude
    private CarConfiguration configuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomOrderStatus status;

    public CustomCarOrder(
            UUID id,
            Client client,
            SalesManager salesManager,
            CarModel carModel,
            CarConfiguration configuration,
            CustomOrderStatus status
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Custom car order id must not be null"));
        this.client = Optional.ofNullable(client)
                .orElseThrow(() -> new DomainValidationException("Client must not be null"));
        this.salesManager = Optional.ofNullable(salesManager)
                .orElseThrow(() -> new DomainValidationException("Sales manager must not be null"));
        this.carModel = Optional.ofNullable(carModel)
                .orElseThrow(() -> new DomainValidationException("Car model must not be null"));
        this.configuration = Optional.ofNullable(configuration)
                .orElseThrow(() -> new DomainValidationException("Car configuration must not be null"));
        this.status = Optional.ofNullable(status)
                .orElseThrow(() -> new DomainValidationException("Custom car order status must not be null"));

        validateConfigurationBelongsToCarModel();
    }

    public void approveByWarehouse() {
        ensureStatus(
                CustomOrderStatus.CREATED,
                "Only a created custom car order can be approved by warehouse"
        );
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

    private void validateConfigurationBelongsToCarModel() {
        if (!configuration.getCarModel().getId().equals(carModel.getId())) {
            throw new DomainValidationException("Configuration car model does not match order car model");
        }
    }

    private void ensureStatus(CustomOrderStatus expected, String message) {
        if (status != expected) {
            throw new DomainValidationException(message);
        }
    }
}