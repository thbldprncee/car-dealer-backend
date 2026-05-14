package org.example.dto.response;

import org.example.domain.enums.ComponentType;

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