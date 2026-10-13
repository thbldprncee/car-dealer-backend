package io.github.thbldprncee.cardealer.storage.dto.request;

import jakarta.validation.constraints.NotNull;
import io.github.thbldprncee.cardealer.events.OrderType;

import java.util.Set;
import java.util.UUID;

public record CreateAssemblyOrderRequest(
        @NotNull UUID sourceOrderId,
        @NotNull OrderType sourceOrderType,
        UUID carId,
        UUID carModelId,
        Set<UUID> requiredComponentOptionIds,
        String traceId
) {
}
