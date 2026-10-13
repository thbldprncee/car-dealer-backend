package io.github.thbldprncee.cardealer.storage.domain.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import io.github.thbldprncee.cardealer.storage.domain.enums.BodyType;
import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;
import io.github.thbldprncee.cardealer.storage.domain.enums.DriveType;
import io.github.thbldprncee.cardealer.storage.domain.enums.FuelType;
import io.github.thbldprncee.cardealer.storage.domain.enums.TransmissionType;
import io.github.thbldprncee.cardealer.storage.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Table(name = "car_models")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class CarModel extends BaseEntity {

    @Column(name = "brand", nullable = false)
    private String brand;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "body_type", nullable = false)
    private BodyType bodyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @Column(name = "engine_power", nullable = false)
    private int enginePower;

    @Column(name = "engine_volume", nullable = false)
    private double engineVolume;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type", nullable = false)
    private TransmissionType transmissionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "drive_type", nullable = false)
    private DriveType driveType;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal basePrice;

    @ElementCollection
    @CollectionTable(
            name = "car_model_required_component_types",
            joinColumns = @JoinColumn(name = "car_model_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false)
    @Getter(AccessLevel.NONE)
    private Set<ComponentType> requiredComponentTypes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "car_model_base_components",
            joinColumns = @JoinColumn(name = "car_model_id"),
            inverseJoinColumns = @JoinColumn(name = "component_option_id")
    )
    @MapKeyColumn(name = "component_type")
    @MapKeyEnumerated(EnumType.STRING)
    @Getter(AccessLevel.NONE)
    private Map<ComponentType, ComponentOption> baseComponents = new EnumMap<>(ComponentType.class);

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
        this.enginePower = validateEnginePower(enginePower);
        this.engineVolume = validateEngineVolume(engineVolume);
        this.transmissionType = Optional.ofNullable(transmissionType)
                .orElseThrow(() -> new DomainValidationException("Transmission type must not be null"));
        this.driveType = Optional.ofNullable(driveType)
                .orElseThrow(() -> new DomainValidationException("Drive type must not be null"));
        this.basePrice = validateBasePrice(basePrice);
        this.requiredComponentTypes = validateRequiredComponentTypes(requiredComponentTypes);
        this.baseComponents = validateBaseComponents(baseComponents);
    }

    public String getDisplayName() {
        return brand + " " + modelName;
    }

    public Set<ComponentType> getRequiredComponentTypes() {
        return Set.copyOf(requiredComponentTypes);
    }

    public Map<ComponentType, ComponentOption> getBaseComponents() {
        return Collections.unmodifiableMap(baseComponents);
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

    private Set<ComponentType> validateRequiredComponentTypes(Set<ComponentType> requiredComponentTypes) {
        Set<ComponentType> validated = Optional.ofNullable(requiredComponentTypes)
                .filter(set -> !set.isEmpty())
                .map(HashSet::new)
                .orElseThrow(() -> new DomainValidationException(
                        "Required component types must not be null or empty"
                ));

        if (validated.stream().anyMatch(type -> type == null)) {
            throw new DomainValidationException("Required component type must not be null");
        }

        return validated;
    }

    private Map<ComponentType, ComponentOption> validateBaseComponents(
            Map<ComponentType, ComponentOption> baseComponents
    ) {
        Map<ComponentType, ComponentOption> validated = new EnumMap<>(ComponentType.class);

        Optional.ofNullable(baseComponents)
                .filter(map -> !map.isEmpty())
                .orElseThrow(() -> new DomainValidationException("Base components must not be null or empty"))
                .forEach((componentType, componentOption) -> {
                    ComponentType validatedType = Optional.ofNullable(componentType)
                            .orElseThrow(() -> new DomainValidationException("Base component type must not be null"));

                    ComponentOption validatedOption = Optional.ofNullable(componentOption)
                            .orElseThrow(() -> new DomainValidationException("Base component option must not be null"));

                    if (validatedOption.getType() != validatedType) {
                        throw new DomainValidationException(
                                "Base component option type does not match map key for component type " + validatedType
                        );
                    }

                    if (!validatedOption.isCompatibleWith(id)) {
                        throw new DomainValidationException(
                                "Base component '%s' is incompatible with car model '%s'"
                                        .formatted(validatedOption.getName(), getDisplayName())
                        );
                    }

                    validated.put(validatedType, validatedOption);
                });

        boolean allRequiredComponentsPresent = requiredComponentTypes.stream()
                .allMatch(validated::containsKey);

        if (!allRequiredComponentsPresent) {
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

    private int validateEnginePower(int value) {
        if (value <= 0) {
            throw new DomainValidationException("Engine power must be greater than zero");
        }

        return value;
    }

    private double validateEngineVolume(double value) {
        if (value <= 0) {
            throw new DomainValidationException("Engine volume must be greater than zero");
        }

        return value;
    }

    private BigDecimal validateBasePrice(BigDecimal value) {
        BigDecimal validated = Optional.ofNullable(value)
                .orElseThrow(() -> new DomainValidationException("Base price must not be null"));

        if (validated.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Base price must not be negative");
        }

        return validated;
    }
}