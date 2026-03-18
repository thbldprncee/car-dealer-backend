package org.example.domain.model;

import org.example.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record SparePart(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Set<UUID> compatibleCarModelIds
) {

    public SparePart {
        id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Spare part id must not be null"));

        name = Optional.ofNullable(name)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException("Spare part name must not be blank"));

        description = Optional.ofNullable(description)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException("Spare part description must not be blank"));

        price = Optional.ofNullable(price)
                .filter(value -> value.compareTo(BigDecimal.ZERO) >= 0)
                .orElseThrow(() -> new DomainValidationException("Spare part price must not be null or negative"));

        compatibleCarModelIds = Set.copyOf(Optional.ofNullable(compatibleCarModelIds)
                .filter(ids -> !ids.isEmpty())
                .orElseThrow(() -> new DomainValidationException(
                        "Compatible car model ids must not be null or empty"
                )));
    }

    public boolean isCompatibleWith(CarModel carModel) {
        return Optional.ofNullable(carModel)
                .map(CarModel::getId)
                .map(compatibleCarModelIds::contains)
                .orElse(false);
    }

    public boolean isCompatibleWith(UUID carModelId) {
        return Optional.ofNullable(carModelId)
                .map(compatibleCarModelIds::contains)
                .orElse(false);
    }
}