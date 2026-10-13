package io.github.thbldprncee.cardealer.storage.dto.response;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record SparePartResponse(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Set<UUID> compatibleCarModelIds
) {
}