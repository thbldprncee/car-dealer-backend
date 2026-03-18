package org.example.dto.request;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TransmissionType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record CreateCarModelRequest(
        UUID id,
        String brand,
        String modelName,
        BodyType bodyType,
        FuelType fuelType,
        int enginePower,
        double engineVolume,
        TransmissionType transmissionType,
        DriveType driveType,
        BigDecimal basePrice,
        Set<ComponentType> requiredComponentTypes,
        Map<ComponentType, UUID> baseComponentIds
) {
}