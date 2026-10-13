package io.github.thbldprncee.cardealer.storage.dto.request;

import io.github.thbldprncee.cardealer.storage.domain.enums.BodyType;
import io.github.thbldprncee.cardealer.storage.domain.enums.CarColor;
import io.github.thbldprncee.cardealer.storage.domain.enums.DriveType;
import io.github.thbldprncee.cardealer.storage.domain.enums.FuelType;
import io.github.thbldprncee.cardealer.storage.domain.enums.TransmissionType;

import java.math.BigDecimal;

public record CarFilterRequest(
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String brand,
        String modelName,
        BodyType bodyType,
        FuelType fuelType,
        Integer minEnginePower,
        Integer maxEnginePower,
        Double minEngineVolume,
        Double maxEngineVolume,
        TransmissionType transmissionType,
        DriveType driveType,
        CarColor color
) {
}