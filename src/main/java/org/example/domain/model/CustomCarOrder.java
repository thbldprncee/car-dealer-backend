package org.example.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.example.domain.enums.CustomOrderStatus;
import org.example.domain.exceptions.DomainValidationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomCarOrder {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final Client client;
    private final SalesManager salesManager;
    private final CarModel carModel;
    private final CarConfiguration configuration;
    private final LocalDateTime createdAt;

    private CustomOrderStatus status;

    public CustomCarOrder(
            UUID id,
            Client client,
            SalesManager salesManager,
            CarModel carModel,
            CarConfiguration configuration,
            LocalDateTime createdAt,
            CustomOrderStatus status
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Custom order id must not be null"));
        this.client = Optional.ofNullable(client)
                .orElseThrow(() -> new DomainValidationException("Client must not be null"));
        this.salesManager = Optional.ofNullable(salesManager)
                .orElseThrow(() -> new DomainValidationException("Sales manager must not be null"));
        this.carModel = Optional.ofNullable(carModel)
                .orElseThrow(() -> new DomainValidationException("Car model must not be null"));
        this.configuration = Optional.ofNullable(configuration)
                .orElseThrow(() -> new DomainValidationException("Car configuration must not be null"));
        this.createdAt = Optional.ofNullable(createdAt)
                .orElseThrow(() -> new DomainValidationException("Created at must not be null"));
        this.status = Optional.ofNullable(status)
                .orElseThrow(() -> new DomainValidationException("Custom order status must not be null"));

        validateConfigurationMatchesModel();
    }

    public void approveByWarehouse() {
        ensureStatus(
                CustomOrderStatus.CREATED,
                "Only an order in CREATED status can be approved by warehouse"
        );
        this.status = CustomOrderStatus.APPROVED_BY_WAREHOUSE;
    }

    public void markWaitingForPayment() {
        ensureStatus(
                CustomOrderStatus.APPROVED_BY_WAREHOUSE,
                "Only an order approved by warehouse can move to WAITING_FOR_PAYMENT"
        );
        this.status = CustomOrderStatus.WAITING_FOR_PAYMENT;
    }

    public void markAsPaid() {
        ensureStatus(
                CustomOrderStatus.WAITING_FOR_PAYMENT,
                "Only an order in WAITING_FOR_PAYMENT status can be marked as PAID"
        );
        this.status = CustomOrderStatus.PAID;
    }

    public void markWaitingForDelivery() {
        ensureStatus(
                CustomOrderStatus.PAID,
                "Only a paid order can move to WAITING_FOR_DELIVERY"
        );
        this.status = CustomOrderStatus.WAITING_FOR_DELIVERY;
    }

    public void markReadyForDelivery() {
        ensureStatus(
                CustomOrderStatus.WAITING_FOR_DELIVERY,
                "Only an order in WAITING_FOR_DELIVERY status can move to READY_FOR_DELIVERY"
        );
        this.status = CustomOrderStatus.READY_FOR_DELIVERY;
    }

    public void complete() {
        ensureStatus(
                CustomOrderStatus.READY_FOR_DELIVERY,
                "Only an order in READY_FOR_DELIVERY status can be completed"
        );
        this.status = CustomOrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == CustomOrderStatus.COMPLETED) {
            throw new DomainValidationException("A completed custom order cannot be canceled");
        }
        if (status == CustomOrderStatus.CANCELED) {
            throw new DomainValidationException("Custom order is already canceled");
        }
        this.status = CustomOrderStatus.CANCELED;
    }

    private void validateConfigurationMatchesModel() {
        if (!configuration.getCarModel().equals(carModel)) {
            throw new DomainValidationException("Configuration car model must match custom order car model");
        }
    }

    private void ensureStatus(CustomOrderStatus expected, String message) {
        if (status != expected) {
            throw new DomainValidationException(message);
        }
    }
}