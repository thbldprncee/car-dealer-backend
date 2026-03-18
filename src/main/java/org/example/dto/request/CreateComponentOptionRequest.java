package org.example.dto.request;

import org.example.domain.enums.ComponentType;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateComponentOptionRequest(
        ComponentType componentType,
        String name,
        BigDecimal priceDelta,
        Set<UUID> compatibleCarModelIds
) {
}