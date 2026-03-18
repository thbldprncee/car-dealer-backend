package org.example.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.example.domain.enums.BodyType;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TransmissionType;
import org.example.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CarModel {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final String brand;
    private final String modelName;
    private final BodyType bodyType;
    private final FuelType fuelType;
    private final int enginePower;
    private final double engineVolume;
    private final TransmissionType transmissionType;
    private final DriveType driveType;
    private final BigDecimal basePrice;

    private final Set<ComponentType> requiredComponentTypes;
    private final Map<ComponentType, ComponentOption> baseComponents;

    public CarModel(
            UUID id,
            String brand,
            String modelName,
            BodyType bodyType,
            FuelType fuelType,
            int enginePower,
            double engineVolume,
            TransmissionType transmissionType,
            DriveType driveType,
            BigDecimal basePrice,
            Set<ComponentType> requiredComponentTypes,
            Map<ComponentType, ComponentOption> baseComponents
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Car model id must not be null"));
        this.brand = validateRequiredText(brand, "Car brand must not be blank");
        this.modelName = validateRequiredText(modelName, "Car model name must not be blank");
        this.bodyType = Optional.ofNullable(bodyType)
                .orElseThrow(() -> new DomainValidationException("Body type must not be null"));
        this.fuelType = Optional.ofNullable(fuelType)
                .orElseThrow(() -> new DomainValidationException("Fuel type must not be null"));
        this.enginePower = validatePositive(enginePower, "Engine power must be greater than zero");
        this.engineVolume = validatePositive(engineVolume, "Engine volume must be greater than zero");
        this.transmissionType = Optional.ofNullable(transmissionType)
                .orElseThrow(() -> new DomainValidationException("Transmission type must not be null"));
        this.driveType = Optional.ofNullable(driveType)
                .orElseThrow(() -> new DomainValidationException("Drive type must not be null"));
        this.basePrice = validateNonNegative(basePrice, "Base price must not be negative");
        this.requiredComponentTypes = Set.copyOf(Optional.ofNullable(requiredComponentTypes)
                .filter(set -> !set.isEmpty())
                .orElseThrow(() -> new DomainValidationException("Required component types must not be null or empty")));
        this.baseComponents = Collections.unmodifiableMap(validateBaseComponents(baseComponents));
    }

    public String getDisplayName() {
        return brand + " " + modelName;
    }

    public Optional<ComponentOption> getBaseComponent(ComponentType componentType) {
        return Optional.ofNullable(componentType)
                .map(baseComponents::get);
    }

    public boolean hasRequiredComponentType(ComponentType componentType) {
        return Optional.ofNullable(componentType)
                .map(requiredComponentTypes::contains)
                .orElse(false);
    }

    private Map<ComponentType, ComponentOption> validateBaseComponents(Map<ComponentType, ComponentOption> baseComponents) {
        Map<ComponentType, ComponentOption> validated = new EnumMap<>(ComponentType.class);

        Optional.ofNullable(baseComponents)
                .filter(map -> !map.isEmpty())
                .orElseThrow(() -> new DomainValidationException("Base components must not be null or empty"))
                .forEach((componentType, componentOption) -> {
                    ComponentType validatedType = Optional.ofNullable(componentType)
                            .orElseThrow(() -> new DomainValidationException("Base component type must not be null"));
                    ComponentOption validatedOption = Optional.ofNullable(componentOption)
                            .orElseThrow(() -> new DomainValidationException("Base component option must not be null"));

                    if (validatedOption.componentType() != validatedType) {
                        throw new DomainValidationException(
                                "Base component option type does not match map key for component type " + validatedType
                        );
                    }

                    if (!validatedOption.isCompatibleWith(id)) {
                        throw new DomainValidationException(
                                "Base component '%s' is incompatible with car model '%s'"
                                        .formatted(validatedOption.name(), getDisplayName())
                        );
                    }

                    validated.put(validatedType, validatedOption);
                });

        boolean allRequiredPresent = requiredComponentTypes.stream()
                .allMatch(validated::containsKey);

        if (!allRequiredPresent) {
            throw new DomainValidationException("Base components must contain all required component types");
        }

        return validated;
    }

    private String validateRequiredText(String value, String message) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException(message));
    }

    private int validatePositive(int value, String message) {
        if (value <= 0) {
            throw new DomainValidationException(message);
        }
        return value;
    }

    private double validatePositive(double value, String message) {
        if (value <= 0) {
            throw new DomainValidationException(message);
        }
        return value;
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