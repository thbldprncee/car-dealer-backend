package io.github.thbldprncee.cardealer.storage.dto.request;

import java.util.UUID;

public record UpdateAssemblyOrderRequest(
        UUID warehouseAdminId,
        Boolean assembled,
        String failureReason
) {
}
