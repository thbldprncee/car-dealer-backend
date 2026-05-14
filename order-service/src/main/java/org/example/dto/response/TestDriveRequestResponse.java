package org.example.dto.response;

import org.example.domain.enums.TestDriveStatus;

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