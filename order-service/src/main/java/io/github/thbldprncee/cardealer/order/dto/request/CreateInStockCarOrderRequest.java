package io.github.thbldprncee.cardealer.order.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInStockCarOrderRequest(
        @NotNull
        UUID carId
) {
}
