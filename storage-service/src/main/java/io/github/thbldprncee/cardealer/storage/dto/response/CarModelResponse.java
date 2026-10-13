package io.github.thbldprncee.cardealer.storage.dto.response;

import io.github.thbldprncee.cardealer.storage.domain.enums.BodyType;
import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;
import io.github.thbldprncee.cardealer.storage.domain.enums.DriveType;
import io.github.thbldprncee.cardealer.storage.domain.enums.FuelType;
import io.github.thbldprncee.cardealer.storage.domain.enums.TransmissionType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record CarModelResponse(
        UUID id,
        String brand,
        String modelName,
        String displayName,
        BodyType bodyType,
        FuelType fuelType,
        int enginePower,
        double engineVolume,
        TransmissionType transmissionType,
        DriveType driveType,
        BigDecimal basePrice,
        Set<ComponentType> requiredComponentTypes,
        Map<ComponentType, ComponentOptionResponse> baseComponents
) {
}