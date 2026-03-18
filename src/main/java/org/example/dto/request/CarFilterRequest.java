package org.example.dto.request;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.CarColor;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TransmissionType;

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