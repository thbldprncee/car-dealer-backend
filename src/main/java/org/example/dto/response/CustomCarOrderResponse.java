package org.example.dto.response;

import org.example.domain.enums.CustomOrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomCarOrderResponse(
        UUID id,
        UUID clientId,
        String clientFullName,
        UUID salesManagerId,
        String salesManagerFullName,
        UUID carModelId,
        String carModelDisplayName,
        UUID carConfigurationId,
        CustomOrderStatus status,
        LocalDateTime createdAt
) {
}