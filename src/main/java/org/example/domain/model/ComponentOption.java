package org.example.domain.model;

import org.example.domain.enums.ComponentType;
import org.example.domain.exceptions.DomainValidationException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public record ComponentOption(
        UUID id,
        ComponentType componentType,
        String name,
        BigDecimal priceDelta,
        Set<UUID> compatibleCarModelIds
) {

    public ComponentOption {
        id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Component option id must not be null"));
        componentType = Optional.ofNullable(componentType)
                .orElseThrow(() -> new DomainValidationException("Component type must not be null"));
        name = Optional.ofNullable(name)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException("Component option name must not be blank"));
        priceDelta = Optional.ofNullable(priceDelta)
                .orElseThrow(() -> new DomainValidationException("Price delta must not be null"));
        compatibleCarModelIds = Set.copyOf(Optional.ofNullable(compatibleCarModelIds)
                .filter(ids -> !ids.isEmpty())
                .orElseThrow(() -> new DomainValidationException("Compatible car model ids must not be null or empty")));
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