package org.example.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.example.domain.enums.CarColor;
import org.example.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Car {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final CarModel carModel;
    private final String vin;
    private CarColor color;
    private BigDecimal price;
    private boolean available;
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
        this.price = validateNonNegative(price, "Car price must not be negative");
        this.available = available;
        this.availableForTestDrive = availableForTestDrive;
    }

    public void updateColor(CarColor color) {
        this.color = Optional.ofNullable(color)
                .orElseThrow(() -> new DomainValidationException("Car color must not be null"));
    }

    public void updatePrice(BigDecimal price) {
        this.price = validateNonNegative(price, "Car price must not be negative");
    }

    public void markAsUnavailable() {
        this.available = false;
    }

    public void markAsAvailable() {
        this.available = true;
    }

    public void enableTestDrive() {
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

    private BigDecimal validateNonNegative(BigDecimal value, String message) {
        BigDecimal validated = Optional.ofNullable(value)
                .orElseThrow(() -> new DomainValidationException(message));

        if (validated.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException(message);
        }

        return validated;
    }
}