package org.example.service;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.CarColor;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TransmissionType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.dto.request.CreateCarModelRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.CreateComponentOptionRequest;
import org.example.dto.request.CreateSparePartRequest;
import org.example.dto.request.UpdateCarRequest;
import org.example.dto.request.UpdateSparePartRequest;
import org.example.dto.response.CarModelResponse;
import org.example.dto.response.CarResponse;
import org.example.dto.response.ComponentOptionResponse;
import org.example.dto.response.SparePartResponse;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.ComponentOptionRepository;
import org.example.repository.SparePartRepository;
import org.example.repository.inmemory.InMemoryCarModelRepository;
import org.example.repository.inmemory.InMemoryCarRepository;
import org.example.repository.inmemory.InMemoryComponentOptionRepository;
import org.example.repository.inmemory.InMemorySparePartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryServiceTest {

    private InventoryService inventoryService;

    private CarRepository carRepository;
    private CarModelRepository carModelRepository;
    private SparePartRepository sparePartRepository;
    private ComponentOptionRepository componentOptionRepository;

    private UUID bmw320iId;
    private UUID bmw330iId;

    private ComponentOptionResponse wheels17;
    private ComponentOptionResponse transmission8at;
    private ComponentOptionResponse steeringStandard;
    private ComponentOptionResponse interiorGraphite;

    private CarModelResponse carModelResponse;
    private CarResponse carResponse;
    private SparePartResponse sparePartResponse;

    @BeforeEach
    void setUp() {
        carRepository = new InMemoryCarRepository();
        carModelRepository = new InMemoryCarModelRepository();
        sparePartRepository = new InMemorySparePartRepository();
        componentOptionRepository = new InMemoryComponentOptionRepository();

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

        interiorGraphite = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.INTERIOR,
                        "Fabric Graphite",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId)
                )
        );

        carModelResponse = inventoryService.createCarModel(
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

        carResponse = inventoryService.createCar(
                new CreateCarRequest(
                        carModelResponse.id(),
                        "VIN-BMW-320I-0001",
                        CarColor.BLACK,
                        new BigDecimal("3650000"),
                        true,
                        true
                )
        );

        sparePartResponse = inventoryService.createSparePart(
                new CreateSparePartRequest(
                        "Front Brake Pads",
                        "Original front brake pads",
                        new BigDecimal("15000"),
                        Set.of(bmw320iId, bmw330iId)
                )
        );
    }

    @Test
    void shouldCreateComponentOption() {
        ComponentOptionResponse response = inventoryService.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.WHEELS,
                        "18 Aero",
                        new BigDecimal("45000"),
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        assertNotNull(response);
        assertEquals(ComponentType.WHEELS, response.componentType());
        assertEquals("18 Aero", response.name());
    }

    @Test
    void shouldCreateCarModel() {
        assertNotNull(carModelResponse);
        assertEquals(bmw320iId, carModelResponse.id());
        assertEquals("BMW", carModelResponse.brand());
        assertEquals("320i", carModelResponse.modelName());
    }

    @Test
    void shouldCreateCar() {
        assertNotNull(carResponse);
        assertEquals("VIN-BMW-320I-0001", carResponse.vin());
        assertEquals(CarColor.BLACK, carResponse.color());
        assertEquals(carModelResponse.id(), carResponse.carModelId());
    }

    @Test
    void shouldCreateSparePart() {
        assertNotNull(sparePartResponse);
        assertEquals("Front Brake Pads", sparePartResponse.name());
        assertEquals(new BigDecimal("15000"), sparePartResponse.price());
    }

    @Test
    void shouldGetCarById() {
        CarResponse response = inventoryService.getCarById(carResponse.id());

        assertEquals(carResponse.id(), response.id());
        assertEquals("VIN-BMW-320I-0001", response.vin());
    }

    @Test
    void shouldGetSparePartById() {
        SparePartResponse response = inventoryService.getSparePartById(sparePartResponse.id());

        assertEquals(sparePartResponse.id(), response.id());
        assertEquals("Front Brake Pads", response.name());
    }

    @Test
    void shouldGetCarModelById() {
        CarModelResponse response = inventoryService.getCarModelById(carModelResponse.id());

        assertEquals(carModelResponse.id(), response.id());
        assertEquals("BMW 320i", response.displayName());
    }

    @Test
    void shouldGetComponentOptionById() {
        ComponentOptionResponse response = inventoryService.getComponentOptionById(wheels17.id());

        assertEquals(wheels17.id(), response.id());
        assertEquals("17 Standard", response.name());
    }

    @Test
    void shouldReturnAllCars() {
        assertEquals(1, inventoryService.getAllCars().size());
    }

    @Test
    void shouldReturnAllSpareParts() {
        assertEquals(1, inventoryService.getAllSpareParts().size());
    }

    @Test
    void shouldReturnAllCarModels() {
        assertEquals(1, inventoryService.getAllCarModels().size());
    }

    @Test
    void shouldReturnAllComponentOptions() {
        assertEquals(4, inventoryService.getAllComponentOptions().size());
    }

    @Test
    void shouldUpdateCar() {
        CarResponse response = inventoryService.updateCar(
                carResponse.id(),
                new UpdateCarRequest(
                        CarColor.WHITE,
                        new BigDecimal("3600000"),
                        false,
                        false
                )
        );

        assertEquals(CarColor.WHITE, response.color());
        assertEquals(new BigDecimal("3600000"), response.price());
        assertFalse(response.available());
        assertFalse(response.availableForTestDrive());
    }

    @Test
    void shouldUpdateSparePart() {
        SparePartResponse response = inventoryService.updateSparePart(
                sparePartResponse.id(),
                new UpdateSparePartRequest(
                        "Rear Brake Pads",
                        "Updated rear brake pads",
                        new BigDecimal("17000"),
                        Set.of(bmw320iId)
                )
        );

        assertEquals("Rear Brake Pads", response.name());
        assertEquals("Updated rear brake pads", response.description());
        assertEquals(new BigDecimal("17000"), response.price());
    }

    @Test
    void shouldDeleteCar() {
        inventoryService.deleteCar(carResponse.id());

        assertEquals(0, inventoryService.getAllCars().size());
    }

    @Test
    void shouldDeleteSparePart() {
        inventoryService.deleteSparePart(sparePartResponse.id());

        assertEquals(0, inventoryService.getAllSpareParts().size());
    }

    @Test
    void shouldDeleteCarModel() {
        inventoryService.deleteCarModel(carModelResponse.id());

        assertEquals(0, inventoryService.getAllCarModels().size());
    }

    @Test
    void shouldDeleteComponentOption() {
        inventoryService.deleteComponentOption(wheels17.id());

        assertEquals(3, inventoryService.getAllComponentOptions().size());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCarModelNotFoundForCarCreation() {
        CreateCarRequest request = new CreateCarRequest(
                UUID.randomUUID(),
                "VIN-NOT-FOUND",
                CarColor.RED,
                new BigDecimal("1000000"),
                true,
                false
        );

        assertThrows(EntityNotFoundException.class, () -> inventoryService.createCar(request));
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenVinAlreadyExists() {
        CreateCarRequest request = new CreateCarRequest(
                carModelResponse.id(),
                "VIN-BMW-320I-0001",
                CarColor.RED,
                new BigDecimal("3500000"),
                true,
                false
        );

        assertThrows(DomainValidationException.class, () -> inventoryService.createCar(request));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingUnknownCar() {
        assertThrows(EntityNotFoundException.class, () -> inventoryService.getCarById(UUID.randomUUID()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingUnknownSparePart() {
        assertThrows(EntityNotFoundException.class, () -> inventoryService.getSparePartById(UUID.randomUUID()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingUnknownCarModel() {
        assertThrows(EntityNotFoundException.class, () -> inventoryService.getCarModelById(UUID.randomUUID()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingUnknownComponentOption() {
        assertThrows(EntityNotFoundException.class, () -> inventoryService.getComponentOptionById(UUID.randomUUID()));
    }
}