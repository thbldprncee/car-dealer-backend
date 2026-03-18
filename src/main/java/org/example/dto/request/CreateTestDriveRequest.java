package org.example.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTestDriveRequest(
        UUID clientId,
        UUID carId,
        LocalDateTime startDateTime
) {
}