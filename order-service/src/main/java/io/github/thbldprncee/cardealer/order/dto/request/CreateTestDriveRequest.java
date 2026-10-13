package io.github.thbldprncee.cardealer.order.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTestDriveRequest(
        @NotNull
        UUID carId,
        @NotNull
        @Future
        LocalDateTime startDateTime
) {
}
