package org.example.service;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TransmissionType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.exceptions.IncompatibleComponentException;
import org.example.dto.request.BuildCarConfigurationRequest;
import org.example.dto.request.CreateCarModelRequest;
import org.example.dto.request.CreateComponentOptionRequest;
import org.example.dto.response.CarConfigurationResponse;
import org.example.dto.response.CarModelResponse;
import org.example.dto.response.ComponentOptionResponse;
import org.example.repository.CarConfigurationRepository;
import org.example.repository.CarModelRepository;
import org.example.repository.ComponentOptionRepository;
import org.example.repository.SparePartRepository;
import org.example.repository.CarRepository;
import org.example.repository.inmemory.InMemoryCarConfigurationRepository;
import org.example.repository.inmemory.InMemoryCarModelRepository;
import org.example.repository.inmemory.InMemoryComponentOptionRepository;
import org.example.repository.inmemory.InMemorySparePartRepository;
import org.example.repository.inmemory.InMemoryCarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguratorServiceTest {

    private ConfiguratorService configuratorService;
    private InventoryService inventoryService;

    private UUID bmw320iId;
    private UUID bmw330iId;

    private ComponentOptionResponse wheels17;
    private ComponentOptionResponse wheels19;
    private ComponentOptionResponse transmission8at;
    private ComponentOptionResponse steeringStandard;
    private ComponentOptionResponse steeringHeated;
    private ComponentOptionResponse interiorGraphite;
    private ComponentOptionResponse interiorDakota;
    private ComponentOptionResponse interiorPerformance;

    private CarModelResponse bmw320i;

    @BeforeEach
    void setUp() {
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        ComponentOptionRepository componentOptionRepository = new InMemoryComponentOptionRepository();
        CarConfigurationRepository carConfigurationRepository = new InMemoryCarConfigurationRepository();

        CarRepository carRepository = new InMemoryCarRepository();
        SparePartRepository sparePartRepository = new InMemorySparePartRepository();

        configuratorService = new ConfiguratorService(
                carModelRepository,
                componentOptionRepository,
                carConfigurationRepository
        );

        inventoryService = new InventoryService(
                carRepository,
                carModelRepository,
                sparePartRepository,
                componentOptionRepository
        );

        bmw320iId = UUID.randomUUID();
        bmw330iId = UUID.randomUUID();

        wheels17 = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.WHEELS,
                        "17 Standard",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId)
                )
        );

        wheels19 = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.WHEELS,
                        "19 M-Sport",
                        new BigDecimal("95000"),
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        transmission8at = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.TRANSMISSION,
                        "Automatic 8AT",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        steeringStandard = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.STEERING_WHEEL,
                        "Sport Leather Standard",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        steeringHeated = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.STEERING_WHEEL,
                        "M-Sport Heated",
                        new BigDecimal("25000"),
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        interiorGraphite = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.INTERIOR,
                        "Fabric Graphite",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId)
                )
        );

        interiorDakota = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.INTERIOR,
                        "Dakota Leather",
                        new BigDecimal("110000"),
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        interiorPerformance = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.INTERIOR,
                        "Performance Interior",
                        new BigDecimal("160000"),
                        Set.of(bmw330iId)
                )
        );

        bmw320i = inventoryService.createCarModel(
                new CreateCarModelRequest(
                        bmw320iId,
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
                        Map.of(
                                ComponentType.WHEELS, wheels17.id(),
                                ComponentType.TRANSMISSION, transmission8at.id(),
                                ComponentType.STEERING_WHEEL, steeringStandard.id(),
                                ComponentType.INTERIOR, interiorGraphite.id()
                        )
                )
        );
    }

    @Test
    void shouldBuildValidConfiguration() {
        BuildCarConfigurationRequest request = new BuildCarConfigurationRequest(
                bmw320i.id(),
                Map.of(
                        ComponentType.WHEELS, wheels19.id(),
                        ComponentType.TRANSMISSION, transmission8at.id(),
                        ComponentType.STEERING_WHEEL, steeringHeated.id(),
                        ComponentType.INTERIOR, interiorDakota.id()
                )
        );

        CarConfigurationResponse response = configuratorService.buildConfiguration(request);

        assertNotNull(response);
        assertEquals(bmw320i.id(), response.carModelId());
        assertEquals("BMW 320i", response.carModelDisplayName());
        assertEquals(new BigDecimal("3730000"), response.totalPrice());
        assertEquals(4, response.selectedComponents().size());
    }

    @Test
    void shouldThrowIncompatibleComponentExceptionWhenComponentIsNotCompatible() {
        BuildCarConfigurationRequest request = new BuildCarConfigurationRequest(
                bmw320i.id(),
                Map.of(
                        ComponentType.WHEELS, wheels19.id(),
                        ComponentType.TRANSMISSION, transmission8at.id(),
                        ComponentType.STEERING_WHEEL, steeringHeated.id(),
                        ComponentType.INTERIOR, interiorPerformance.id()
                )
        );

        assertThrows(IncompatibleComponentException.class,
                () -> configuratorService.buildConfiguration(request));
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenRequiredComponentIsMissing() {
        BuildCarConfigurationRequest request = new BuildCarConfigurationRequest(
                bmw320i.id(),
                Map.of(
                        ComponentType.WHEELS, wheels19.id(),
                        ComponentType.TRANSMISSION, transmission8at.id(),
                        ComponentType.STEERING_WHEEL, steeringHeated.id()
                )
        );

        assertThrows(DomainValidationException.class,
                () -> configuratorService.buildConfiguration(request));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCarModelDoesNotExist() {
        BuildCarConfigurationRequest request = new BuildCarConfigurationRequest(
                UUID.randomUUID(),
                Map.of(
                        ComponentType.WHEELS, wheels19.id(),
                        ComponentType.TRANSMISSION, transmission8at.id(),
                        ComponentType.STEERING_WHEEL, steeringHeated.id(),
                        ComponentType.INTERIOR, interiorDakota.id()
                )
        );

        assertThrows(EntityNotFoundException.class,
                () -> configuratorService.buildConfiguration(request));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenComponentOptionDoesNotExist() {
        BuildCarConfigurationRequest request = new BuildCarConfigurationRequest(
                bmw320i.id(),
                Map.of(
                        ComponentType.WHEELS, UUID.randomUUID(),
                        ComponentType.TRANSMISSION, transmission8at.id(),
                        ComponentType.STEERING_WHEEL, steeringHeated.id(),
                        ComponentType.INTERIOR, interiorDakota.id()
                )
        );

        assertThrows(EntityNotFoundException.class,
                () -> configuratorService.buildConfiguration(request));
    }
}