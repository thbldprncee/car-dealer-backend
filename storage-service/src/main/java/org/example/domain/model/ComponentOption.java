package org.example.domain.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.domain.enums.ComponentType;
import org.example.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Table(name = "component_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class ComponentOption extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ComponentType type;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price_delta", nullable = false, precision = 19, scale = 2)
    private BigDecimal priceDelta;

    @ElementCollection
    @CollectionTable(
            name = "component_option_compatible_car_models",
            joinColumns = @JoinColumn(name = "component_option_id")
    )
    @Column(name = "car_model_id", nullable = false)
    @Getter(AccessLevel.NONE)
    private Set<UUID> compatibleCarModelIds = new HashSet<>();

    public ComponentOption(
            UUID id,
            ComponentType type,
            String name,
            BigDecimal priceDelta,
            Set<UUID> compatibleCarModelIds
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Component option id must not be null"));
        this.type = Optional.ofNullable(type)
                .orElseThrow(() -> new DomainValidationException("Component type must not be null"));
        this.name = validateRequiredText(name, "Component option name must not be blank");
        this.priceDelta = Optional.ofNullable(priceDelta)
                .orElseThrow(() -> new DomainValidationException("Component option price delta must not be null"));
        this.compatibleCarModelIds = validateCompatibleCarModelIds(compatibleCarModelIds);
    }

    public Set<UUID> getCompatibleCarModelIds() {
        return Set.copyOf(compatibleCarModelIds);
    }

    public boolean isCompatibleWith(CarModel carModel) {
        if (carModel == null) {
            throw new DomainValidationException("Car model must not be null");
        }

        return compatibleCarModelIds.contains(carModel.getId());
    }

    public boolean isCompatibleWith(UUID carModelId) {
        if (carModelId == null) {
            throw new DomainValidationException("Car model id must not be null");
        }

        return compatibleCarModelIds.contains(carModelId);
    }

    private String validateRequiredText(String value, String message) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException(message));
    }

    private Set<UUID> validateCompatibleCarModelIds(Set<UUID> compatibleCarModelIds) {
        if (compatibleCarModelIds == null || compatibleCarModelIds.isEmpty()) {
            throw new DomainValidationException("Compatible car model ids must not be empty");
        }

        if (compatibleCarModelIds.stream().anyMatch(id -> id == null)) {
            throw new DomainValidationException("Compatible car model id must not be null");
        }

        return new HashSet<>(compatibleCarModelIds);
    }
}