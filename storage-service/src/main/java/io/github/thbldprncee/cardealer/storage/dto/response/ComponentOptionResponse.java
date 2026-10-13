package io.github.thbldprncee.cardealer.storage.dto.response;

import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ComponentOptionResponse(
        UUID id,
        ComponentType componentType,
        String name,
        BigDecimal priceDelta,
        Set<UUID> compatibleCarModelIds
) {
}