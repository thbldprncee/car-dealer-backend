package io.github.thbldprncee.cardealer.storage.dto.request;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record UpdateSparePartRequest(
        String name,
        String description,
        BigDecimal price,
        Set<UUID> compatibleCarModelIds
) {
}