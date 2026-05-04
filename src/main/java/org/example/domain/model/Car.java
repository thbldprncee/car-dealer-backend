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
import org.example.domain.enums.CarColor;
import org.example.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Getter
@Entity
@Table(name = "cars")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Car extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_model_id", nullable = false)
    @ToString.Exclude
    private CarModel carModel;

    @Column(name = "vin", nullable = false, unique = true)
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false)
    private CarColor color;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "available", nullable = false)
    private boolean available;

    @Column(name = "available_for_test_drive", nullable = false)
    private boolean availableForTestDrive;

    public Car(
            UUID id,
            CarModel carModel,
            String vin,
            CarColor color,
            BigDecimal price,
            boolean available,
            boolean availableForTestDrive
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Car id must not be null"));
        this.carModel = Optional.ofNullable(carModel)
                .orElseThrow(() -> new DomainValidationException("Car model must not be null"));
        this.vin = validateRequiredText(vin, "VIN must not be blank");
        this.color = Optional.ofNullable(color)
                .orElseThrow(() -> new DomainValidationException("Car color must not be null"));
        this.price = validateCarPrice(price);
        this.available = available;
        this.availableForTestDrive = availableForTestDrive;
    }

    public void updateColor(CarColor color) {
        this.color = Optional.ofNullable(color)
                .orElseThrow(() -> new DomainValidationException("Car color must not be null"));
    }

    public void updatePrice(BigDecimal price) {
        this.price = validateCarPrice(price);
    }

    public void markAsUnavailable() {
        this.available = false;
    }

    public void markAsAvailable() {
        this.available = true;
    }

    public void enableTestDrive() {
        if (!available) {
            throw new DomainValidationException("Unavailable car cannot be added to test drive list");
        }

        this.availableForTestDrive = true;
    }

    public void disableTestDrive() {
        this.availableForTestDrive = false;
    }

    public String getDisplayName() {
        return carModel.getDisplayName() + " (" + vin + ")";
    }

    private String validateRequiredText(String value, String message) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException(message));
    }

    private BigDecimal validateCarPrice(BigDecimal value) {
        BigDecimal validated = Optional.ofNullable(value)
                .orElseThrow(() -> new DomainValidationException("Car price must not be null"));

        if (validated.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Car price must not be negative");
        }

        return validated;
    }
}