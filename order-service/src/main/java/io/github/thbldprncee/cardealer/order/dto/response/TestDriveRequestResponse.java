package io.github.thbldprncee.cardealer.order.dto.response;

import io.github.thbldprncee.cardealer.order.domain.enums.TestDriveStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TestDriveRequestResponse(
        UUID id,
        UUID clientId,
        String clientFullName,
        UUID carId,
        String carDisplayName,
        LocalDateTime startDateTime,
        TestDriveStatus status
) {
}