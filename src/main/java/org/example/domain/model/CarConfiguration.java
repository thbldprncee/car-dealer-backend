package org.example.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.example.domain.enums.ComponentType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.IncompatibleComponentException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CarConfiguration {

    @EqualsAndHashCode.Include
    private final UUID id;

    private final CarModel carModel;
    private final Map<ComponentType, ComponentOption> selectedComponents;
    private final BigDecimal totalPrice;

    public CarConfiguration(
            UUID id,
            CarModel carModel,
            Map<ComponentType, ComponentOption> selectedComponents
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Car configuration id must not be null"));
        this.carModel = Optional.ofNullable(carModel)
                .orElseThrow(() -> new DomainValidationException("Car model must not be null"));
        this.selectedComponents = Collections.unmodifiableMap(validateSelectedComponents(selectedComponents));
        this.totalPrice = calculateTotalPrice();
    }

    public Optional<ComponentOption> getSelectedComponent(ComponentType componentType) {
        return Optional.ofNullable(componentType)
                .map(selectedComponents::get);
    }

    public boolean containsComponentType(ComponentType componentType) {
        return Optional.ofNullable(componentType)
                .map(selectedComponents::containsKey)
                .orElse(false);
    }

    private Map<ComponentType, ComponentOption> validateSelectedComponents(
            Map<ComponentType, ComponentOption> selectedComponents
    ) {
        Map<ComponentType, ComponentOption> validated = new EnumMap<>(ComponentType.class);

        Optional.ofNullable(selectedComponents)
                .filter(map -> !map.isEmpty())
                .orElseThrow(() -> new DomainValidationException("Selected components must not be null or empty"))
                .forEach((componentType, componentOption) -> {
                    ComponentType validatedType = Optional.ofNullable(componentType)
                            .orElseThrow(() -> new DomainValidationException("Selected component type must not be null"));

                    ComponentOption validatedOption = Optional.ofNullable(componentOption)
                            .orElseThrow(() -> new DomainValidationException("Selected component option must not be null"));

                    if (validatedOption.componentType() != validatedType) {
                        throw new DomainValidationException(
                                "Selected component option type does not match map key for component type " + validatedType
                        );
                    }

                    if (!validatedOption.isCompatibleWith(carModel)) {
                        throw new IncompatibleComponentException(
                                "Selected component '%s' is incompatible with car model '%s'"
                                        .formatted(validatedOption.name(), carModel.getDisplayName())
                        );
                    }

                    validated.put(validatedType, validatedOption);
                });

        boolean allRequiredComponentsPresent = carModel.getRequiredComponentTypes().stream()
                .allMatch(validated::containsKey);

        if (!allRequiredComponentsPresent) {
            throw new DomainValidationException("Selected components must contain all required component types");
        }

        return validated;
    }

    private BigDecimal calculateTotalPrice() {
        BigDecimal additionalPrice = selectedComponents.values().stream()
                .map(ComponentOption::priceDelta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return carModel.getBasePrice().add(additionalPrice);
    }

    public CarModel getCarModel() {
        return carModel;
    }
}