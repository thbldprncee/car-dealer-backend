package org.example.domain.model;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TransmissionType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.IncompatibleComponentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CarConfigurationTest {

    private CarModel carModel;
    private ComponentOption wheels;
    private ComponentOption transmission;
    private ComponentOption steering;
    private ComponentOption interior;
    private ComponentOption incompatibleInterior;

    @BeforeEach
    void setUp() {
        UUID carModelId = UUID.randomUUID();
        UUID anotherModelId = UUID.randomUUID();

        wheels = new ComponentOption(
                UUID.randomUUID(),
                ComponentType.WHEELS,
                "17 Standard",
                BigDecimal.ZERO,
                Set.of(carModelId)
        );

        transmission = new ComponentOption(
                UUID.randomUUID(),
                ComponentType.TRANSMISSION,
                "Automatic 8AT",
                BigDecimal.ZERO,
                Set.of(carModelId)
        );

        steering = new ComponentOption(
                UUID.randomUUID(),
                ComponentType.STEERING_WHEEL,
                "Sport Leather Standard",
                BigDecimal.ZERO,
                Set.of(carModelId)
        );

        interior = new ComponentOption(
                UUID.randomUUID(),
                ComponentType.INTERIOR,
                "Dakota Leather",
                new BigDecimal("110000"),
                Set.of(carModelId)
        );

        incompatibleInterior = new ComponentOption(
                UUID.randomUUID(),
                ComponentType.INTERIOR,
                "Performance Interior",
                new BigDecimal("160000"),
                Set.of(anotherModelId)
        );

        Map<ComponentType, ComponentOption> baseComponents = new EnumMap<>(ComponentType.class);
        baseComponents.put(ComponentType.WHEELS, wheels);
        baseComponents.put(ComponentType.TRANSMISSION, transmission);
        baseComponents.put(ComponentType.STEERING_WHEEL, steering);
        baseComponents.put(ComponentType.INTERIOR, interior);

        carModel = new CarModel(
                carModelId,
                "BMW",
                "320i",
                BodyType.SEDAN,
                FuelType.PETROL,
                184,
                2.0,
                TransmissionType.AUTOMATIC,
                DriveType.REAR_WHEEL,
                new BigDecimal("3500000"),
                Set.of(
                        ComponentType.WHEELS,
                        ComponentType.TRANSMISSION,
                        ComponentType.STEERING_WHEEL,
                        ComponentType.INTERIOR
                ),
                baseComponents
        );
    }

    @Test
    void shouldCreateConfigurationAndCalculateTotalPrice() {
        Map<ComponentType, ComponentOption> selected = new EnumMap<>(ComponentType.class);
        selected.put(ComponentType.WHEELS, wheels);
        selected.put(ComponentType.TRANSMISSION, transmission);
        selected.put(ComponentType.STEERING_WHEEL, steering);
        selected.put(ComponentType.INTERIOR, interior);

        CarConfiguration configuration = new CarConfiguration(
                UUID.randomUUID(),
                carModel,
                selected
        );

        assertEquals(carModel, configuration.getCarModel());
        assertEquals(new BigDecimal("3610000"), configuration.getTotalPrice());
        assertTrue(configuration.containsComponentType(ComponentType.INTERIOR));
        assertTrue(configuration.getSelectedComponent(ComponentType.INTERIOR).isPresent());
    }

    @Test
    void shouldThrowWhenRequiredComponentIsMissing() {
        Map<ComponentType, ComponentOption> selected = new EnumMap<>(ComponentType.class);
        selected.put(ComponentType.WHEELS, wheels);
        selected.put(ComponentType.TRANSMISSION, transmission);
        selected.put(ComponentType.STEERING_WHEEL, steering);

        assertThrows(DomainValidationException.class,
                () -> new CarConfiguration(UUID.randomUUID(), carModel, selected));
    }

    @Test
    void shouldThrowWhenComponentIsIncompatible() {
        Map<ComponentType, ComponentOption> selected = new EnumMap<>(ComponentType.class);
        selected.put(ComponentType.WHEELS, wheels);
        selected.put(ComponentType.TRANSMISSION, transmission);
        selected.put(ComponentType.STEERING_WHEEL, steering);
        selected.put(ComponentType.INTERIOR, incompatibleInterior);

        assertThrows(IncompatibleComponentException.class,
                () -> new CarConfiguration(UUID.randomUUID(), carModel, selected));
    }
}