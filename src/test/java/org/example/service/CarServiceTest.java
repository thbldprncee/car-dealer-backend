package org.example.service;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.CarColor;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TransmissionType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.model.Car;
import org.example.domain.model.CarModel;
import org.example.dto.request.CarFilterRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.UpdateCarRequest;
import org.example.dto.response.CarResponse;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.inmemory.InMemoryCarModelRepository;
import org.example.repository.inmemory.InMemoryCarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CarServiceTest {

    private CarService carService;
    private CarRepository carRepository;
    private CarModelRepository carModelRepository;

    private CarModel bmw320i;
    private CarModel ladaVesta;

    private Car blackBmw;
    private Car whiteBmw;
    private Car redLada;

    @BeforeEach
    void setUp() {
        carRepository = new InMemoryCarRepository();
        carModelRepository = new InMemoryCarModelRepository();

        carService = new CarService(carRepository, carModelRepository);

        bmw320i = createCarModel(
                UUID.randomUUID(),
                "BMW",
                "320i",
                BodyType.SEDAN,
                FuelType.PETROL,
                184,
                2.0,
                TransmissionType.AUTOMATIC,
                DriveType.REAR_WHEEL,
                new BigDecimal("3500000")
        );

        ladaVesta = createCarModel(
                UUID.randomUUID(),
                "Lada",
                "Vesta",
                BodyType.SEDAN,
                FuelType.PETROL,
                106,
                1.6,
                TransmissionType.MANUAL,
                DriveType.FRONT_WHEEL,
                new BigDecimal("1700000")
        );

        carModelRepository.save(bmw320i);
        carModelRepository.save(ladaVesta);

        blackBmw = new Car(
                UUID.randomUUID(),
                bmw320i,
                "VIN-BMW-0001",
                CarColor.BLACK,
                new BigDecimal("3650000"),
                true,
                true
        );

        whiteBmw = new Car(
                UUID.randomUUID(),
                bmw320i,
                "VIN-BMW-0002",
                CarColor.WHITE,
                new BigDecimal("3620000"),
                true,
                false
        );

        redLada = new Car(
                UUID.randomUUID(),
                ladaVesta,
                "VIN-LADA-0001",
                CarColor.RED,
                new BigDecimal("1750000"),
                false,
                false
        );

        carRepository.save(blackBmw);
        carRepository.save(whiteBmw);
        carRepository.save(redLada);
    }

    @Test
    void shouldCreateCar() {
        CreateCarRequest request = new CreateCarRequest(
                bmw320i.getId(),
                "VIN-BMW-0003",
                CarColor.GRAY,
                new BigDecimal("3700000"),
                true,
                true
        );

        CarResponse response = carService.createCar(request);

        assertNotNull(response);
        assertEquals("VIN-BMW-0003", response.vin());
        assertEquals(CarColor.GRAY, response.color());
        assertEquals(bmw320i.getId(), response.carModelId());
        assertEquals("BMW 320i", response.carModelDisplayName());
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenVinAlreadyExists() {
        CreateCarRequest request = new CreateCarRequest(
                bmw320i.getId(),
                "VIN-BMW-0001",
                CarColor.GRAY,
                new BigDecimal("3700000"),
                true,
                true
        );

        assertThrows(DomainValidationException.class, () -> carService.createCar(request));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCarModelDoesNotExist() {
        CreateCarRequest request = new CreateCarRequest(
                UUID.randomUUID(),
                "VIN-UNKNOWN-0001",
                CarColor.GRAY,
                new BigDecimal("3700000"),
                true,
                true
        );

        assertThrows(EntityNotFoundException.class, () -> carService.createCar(request));
    }

    @Test
    void shouldGetCarById() {
        CarResponse response = carService.getCarById(blackBmw.getId());

        assertEquals(blackBmw.getId(), response.id());
        assertEquals("VIN-BMW-0001", response.vin());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingUnknownCar() {
        assertThrows(EntityNotFoundException.class, () -> carService.getCarById(UUID.randomUUID()));
    }

    @Test
    void shouldReturnAllCars() {
        assertEquals(3, carService.getAllCars().size());
    }

    @Test
    void shouldReturnOnlyAvailableCars() {
        assertEquals(2, carService.getAvailableCars().size());
    }

    @Test
    void shouldReturnOnlyCarsAvailableForTestDrive() {
        assertEquals(1, carService.getCarsAvailableForTestDrive().size());
        assertEquals(blackBmw.getId(), carService.getCarsAvailableForTestDrive().get(0).id());
    }

    @Test
    void shouldFilterByBrandAndModel() {
        CarFilterRequest filter = new CarFilterRequest(
                null,
                null,
                "BMW",
                "320i",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(2, carService.filterCars(filter).size());
    }

    @Test
    void shouldIgnoreModelFilterWhenBrandIsNotSpecified() {
        CarFilterRequest filter = new CarFilterRequest(
                null,
                null,
                null,
                "320i",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(2, carService.filterCars(filter).size());
    }

    @Test
    void shouldFilterByColor() {
        CarFilterRequest filter = new CarFilterRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                CarColor.BLACK
        );

        assertEquals(1, carService.filterCars(filter).size());
        assertEquals(blackBmw.getId(), carService.filterCars(filter).get(0).id());
    }

    @Test
    void shouldFilterByPriceRange() {
        CarFilterRequest filter = new CarFilterRequest(
                new BigDecimal("3600000"),
                new BigDecimal("3660000"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertEquals(2, carService.filterCars(filter).size());
    }

    @Test
    void shouldFilterByBodyTypeFuelTransmissionAndDrive() {
        CarFilterRequest filter = new CarFilterRequest(
                null,
                null,
                null,
                null,
                BodyType.SEDAN,
                FuelType.PETROL,
                null,
                null,
                null,
                null,
                TransmissionType.AUTOMATIC,
                DriveType.REAR_WHEEL,
                null
        );

        assertEquals(2, carService.filterCars(filter).size());
    }

    @Test
    void shouldUpdateCar() {
        UpdateCarRequest request = new UpdateCarRequest(
                CarColor.BLUE,
                new BigDecimal("3550000"),
                false,
                false
        );

        CarResponse response = carService.updateCar(whiteBmw.getId(), request);

        assertEquals(CarColor.BLUE, response.color());
        assertEquals(new BigDecimal("3550000"), response.price());
        assertFalse(response.available());
        assertFalse(response.availableForTestDrive());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingUnknownCar() {
        UpdateCarRequest request = new UpdateCarRequest(
                CarColor.BLUE,
                new BigDecimal("3550000"),
                false,
                false
        );

        assertThrows(EntityNotFoundException.class, () -> carService.updateCar(UUID.randomUUID(), request));
    }

    @Test
    void shouldDeleteCar() {
        carService.deleteCar(whiteBmw.getId());

        assertEquals(2, carService.getAllCars().size());
        assertFalse(carRepository.existsById(whiteBmw.getId()));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingUnknownCar() {
        assertThrows(EntityNotFoundException.class, () -> carService.deleteCar(UUID.randomUUID()));
    }

    private CarModel createCarModel(
            UUID id,
            String brand,
            String modelName,
            BodyType bodyType,
            FuelType fuelType,
            int enginePower,
            double engineVolume,
            TransmissionType transmissionType,
            DriveType driveType,
            BigDecimal basePrice
    ) {
        org.example.domain.model.ComponentOption wheels = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.WHEELS,
                "Standard Wheels",
                BigDecimal.ZERO,
                Set.of(id)
        );

        org.example.domain.model.ComponentOption transmission = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.TRANSMISSION,
                "Standard Transmission",
                BigDecimal.ZERO,
                Set.of(id)
        );

        org.example.domain.model.ComponentOption steering = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.STEERING_WHEEL,
                "Standard Steering",
                BigDecimal.ZERO,
                Set.of(id)
        );

        org.example.domain.model.ComponentOption interior = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.INTERIOR,
                "Standard Interior",
                BigDecimal.ZERO,
                Set.of(id)
        );

        Map<ComponentType, org.example.domain.model.ComponentOption> baseComponents = new EnumMap<>(ComponentType.class);
        baseComponents.put(ComponentType.WHEELS, wheels);
        baseComponents.put(ComponentType.TRANSMISSION, transmission);
        baseComponents.put(ComponentType.STEERING_WHEEL, steering);
        baseComponents.put(ComponentType.INTERIOR, interior);

        return new CarModel(
                id,
                brand,
                modelName,
                bodyType,
                fuelType,
                enginePower,
                engineVolume,
                transmissionType,
                driveType,
                basePrice,
                Set.of(
                        ComponentType.WHEELS,
                        ComponentType.TRANSMISSION,
                        ComponentType.STEERING_WHEEL,
                        ComponentType.INTERIOR
                ),
                baseComponents
        );
    }
}