package org.example.dto.request;

import java.util.Set;
import java.util.UUID;

public record CarModelFilterRequest(
        String brand,
        Set<UUID> componentOptionIds
) {
}