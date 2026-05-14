package org.example.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Entity
@Table(name = "car_configurations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class CarConfiguration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_model_id", nullable = false)
    @ToString.Exclude
    private CarModel carModel;

    @ManyToMany
    @JoinTable(
            name = "car_configuration_selected_components",
            joinColumns = @JoinColumn(name = "car_configuration_id"),
            inverseJoinColumns = @JoinColumn(name = "component_option_id")
    )
    @MapKeyColumn(name = "component_type")
    @MapKeyEnumerated(EnumType.STRING)
    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    private Map<ComponentType, ComponentOption> selectedComponents = new EnumMap<>(ComponentType.class);

    public CarConfiguration(
            UUID id,
            CarModel carModel,
            Map<ComponentType, ComponentOption> selectedComponents
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Car configuration id must not be null"));
        this.carModel = Optional.ofNullable(carModel)
                .orElseThrow(() -> new DomainValidationException("Car model must not be null"));
        this.selectedComponents = validateSelectedComponents(selectedComponents);
    }

    public Map<ComponentType, ComponentOption> getSelectedComponents() {
        return Collections.unmodifiableMap(selectedComponents);
    }

    public BigDecimal calculateTotalPrice() {
        BigDecimal componentsPrice = selectedComponents.values().stream()
                .map(ComponentOption::getPriceDelta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return carModel.getBasePrice().add(componentsPrice);
    }

    public ComponentOption getSelectedComponent(ComponentType componentType) {
        if (componentType == null) {
            throw new DomainValidationException("Component type must not be null");
        }

        ComponentOption componentOption = selectedComponents.get(componentType);

        if (componentOption == null) {
            throw new DomainValidationException("Component option for type " + componentType + " is not selected");
        }

        return componentOption;
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
                            .orElseThrow(() -> new DomainValidationException("Component type must not be null"));

                    ComponentOption validatedOption = Optional.ofNullable(componentOption)
                            .orElseThrow(() -> new DomainValidationException("Component option must not be null"));

                    if (validatedOption.getType() != validatedType) {
                        throw new DomainValidationException(
                                "Selected component option type does not match map key for component type "
                                        + validatedType
                        );
                    }

                    if (!validatedOption.isCompatibleWith(carModel)) {
                        throw new IncompatibleComponentException(
                                "Component '%s' is incompatible with car model '%s'"
                                        .formatted(validatedOption.getName(), carModel.getDisplayName())
                        );
                    }

                    validated.put(validatedType, validatedOption);
                });

        carModel.getRequiredComponentTypes().forEach(requiredType -> {
            if (!validated.containsKey(requiredType)) {
                throw new DomainValidationException(
                        "Required component type '%s' is missing".formatted(requiredType)
                );
            }
        });

        return validated;
    }
}