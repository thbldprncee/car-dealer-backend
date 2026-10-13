package io.github.thbldprncee.cardealer.storage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import io.github.thbldprncee.cardealer.storage.domain.enums.BodyType;
import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;
import io.github.thbldprncee.cardealer.storage.domain.enums.DriveType;
import io.github.thbldprncee.cardealer.storage.domain.enums.FuelType;
import io.github.thbldprncee.cardealer.storage.domain.enums.TransmissionType;

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
