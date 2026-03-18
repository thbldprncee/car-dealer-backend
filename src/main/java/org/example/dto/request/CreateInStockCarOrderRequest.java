package org.example.dto.request;

import java.util.UUID;

public record CreateInStockCarOrderRequest(
        UUID clientId,
        UUID carId
) {
}