package org.example.dto.request;

import org.example.domain.enums.ComponentType;

import java.util.Map;
import java.util.UUID;

public record BuildCarConfigurationRequest(
        UUID carModelId,
        Map<ComponentType, UUID> selectedComponentOptionIds
) {
}