package org.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
        @NotNull
        UUID id,
        @NotBlank
        String brand,
        @NotBlank
        String modelName,
        @NotNull
        BodyType bodyType,
        @NotNull
        FuelType fuelType,
        @Positive
        int enginePower,
        @Positive
        double engineVolume,
        @NotNull
        TransmissionType transmissionType,
        @NotNull
        DriveType driveType,
        @NotNull
        @PositiveOrZero
        BigDecimal basePrice,
        @NotEmpty
        Set<ComponentType> requiredComponentTypes,
        @NotEmpty
        Map<ComponentType, UUID> baseComponentIds
) {
}
