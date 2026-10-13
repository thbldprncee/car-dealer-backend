package io.github.thbldprncee.cardealer.storage.dto.response;

import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record CarConfigurationResponse(
        UUID id,
        UUID carModelId,
        String carModelDisplayName,
        Map<ComponentType, ComponentOptionResponse> selectedComponents,
        BigDecimal totalPrice
) {
}