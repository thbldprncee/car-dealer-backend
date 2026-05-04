package org.example.domain.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Table(name = "spare_parts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class SparePart extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @ElementCollection
    @CollectionTable(
            name = "spare_part_compatible_car_models",
            joinColumns = @JoinColumn(name = "spare_part_id")
    )
    @Column(name = "car_model_id", nullable = false)
    @Getter(AccessLevel.NONE)
    private Set<UUID> compatibleCarModelIds = new HashSet<>();

    public SparePart(
            UUID id,
            String name,
            String description,
            BigDecimal price,
            Set<UUID> compatibleCarModelIds
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Spare part id must not be null"));
        this.name = validateRequiredText(name, "Spare part name must not be blank");
        this.description = validateRequiredText(description, "Spare part description must not be blank");
        this.price = validatePrice(price);
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

    public void update(
            String name,
            String description,
            BigDecimal price,
            Set<UUID> compatibleCarModelIds
    ) {
        this.name = validateRequiredText(name, "Spare part name must not be blank");
        this.description = validateRequiredText(description, "Spare part description must not be blank");
        this.price = validatePrice(price);
        this.compatibleCarModelIds.clear();
        this.compatibleCarModelIds.addAll(validateCompatibleCarModelIds(compatibleCarModelIds));
    }

    private String validateRequiredText(String value, String message) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException(message));
    }

    private BigDecimal validatePrice(BigDecimal value) {
        BigDecimal validated = Optional.ofNullable(value)
                .orElseThrow(() -> new DomainValidationException("Spare part price must not be null"));

        if (validated.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainValidationException("Spare part price must not be negative");
        }

        return validated;
    }

    private Set<UUID> validateCompatibleCarModelIds(Set<UUID> compatibleCarModelIds) {
        if (compatibleCarModelIds == null || compatibleCarModelIds.isEmpty()) {
            throw new DomainValidationException("Compatible car model ids must not be null or empty");
        }

        if (compatibleCarModelIds.stream().anyMatch(id -> id == null)) {
            throw new DomainValidationException("Compatible car model id must not be null");
        }

        return new HashSet<>(compatibleCarModelIds);
    }
}