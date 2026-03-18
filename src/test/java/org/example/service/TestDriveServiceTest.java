package org.example.service;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.CarColor;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TestDriveStatus;
import org.example.domain.enums.TransmissionType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.model.Car;
import org.example.domain.model.CarModel;
import org.example.domain.model.Client;
import org.example.dto.request.CreateTestDriveRequest;
import org.example.dto.response.CarResponse;
import org.example.dto.response.TestDriveRequestResponse;
import org.example.repository.CarRepository;
import org.example.repository.TestDriveRequestRepository;
import org.example.repository.UserRepository;
import org.example.repository.inmemory.InMemoryCarRepository;
import org.example.repository.inmemory.InMemoryTestDriveRequestRepository;
import org.example.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestDriveServiceTest {

    private TestDriveService testDriveService;

    private UserRepository userRepository;
    private CarRepository carRepository;
    private TestDriveRequestRepository testDriveRequestRepository;

    private Client client;
    private Car availableTestDriveCar;
    private Car unavailableTestDriveCar;
    private Car notForTestDriveCar;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        carRepository = new InMemoryCarRepository();
        testDriveRequestRepository = new InMemoryTestDriveRequestRepository();

        testDriveService = new TestDriveService(
                userRepository,
                carRepository,
                testDriveRequestRepository
        );

        client = new Client(
                UUID.randomUUID(),
                "Ivan",
                "Petrov",
                "ivan.petrov@example.com"
        );
        userRepository.save(client);

        UUID carModelId = UUID.randomUUID();

        org.example.domain.model.ComponentOption wheels = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.WHEELS,
                "17 Standard",
                BigDecimal.ZERO,
                Set.of(carModelId)
        );

        org.example.domain.model.ComponentOption transmission = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.TRANSMISSION,
                "Automatic 8AT",
                BigDecimal.ZERO,
                Set.of(carModelId)
        );

        org.example.domain.model.ComponentOption steering = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.STEERING_WHEEL,
                "Sport Leather Standard",
                BigDecimal.ZERO,
                Set.of(carModelId)
        );

        org.example.domain.model.ComponentOption interior = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.INTERIOR,
                "Fabric Graphite",
                BigDecimal.ZERO,
                Set.of(carModelId)
        );

        Map<ComponentType, org.example.domain.model.ComponentOption> baseComponents = new EnumMap<>(ComponentType.class);
        baseComponents.put(ComponentType.WHEELS, wheels);
        baseComponents.put(ComponentType.TRANSMISSION, transmission);
        baseComponents.put(ComponentType.STEERING_WHEEL, steering);
        baseComponents.put(ComponentType.INTERIOR, interior);

        CarModel carModel = new CarModel(
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

        availableTestDriveCar = new Car(
                UUID.randomUUID(),
                carModel,
                "VIN-BMW-320I-0001",
                CarColor.BLACK,
                new BigDecimal("3650000"),
                true,
                true
        );

        unavailableTestDriveCar = new Car(
                UUID.randomUUID(),
                carModel,
                "VIN-BMW-320I-0002",
                CarColor.WHITE,
                new BigDecimal("3620000"),
                false,
                true
        );

        notForTestDriveCar = new Car(
                UUID.randomUUID(),
                carModel,
                "VIN-BMW-320I-0003",
                CarColor.RED,
                new BigDecimal("3600000"),
                true,
                false
        );

        carRepository.save(availableTestDriveCar);
        carRepository.save(unavailableTestDriveCar);
        carRepository.save(notForTestDriveCar);
    }

    @Test
    void shouldCreateTestDriveRequest() {
        CreateTestDriveRequest request = new CreateTestDriveRequest(
                client.getId(),
                availableTestDriveCar.getId(),
                LocalDateTime.now().plusDays(1)
        );

        TestDriveRequestResponse response = testDriveService.createTestDriveRequest(request);

        assertNotNull(response);
        assertEquals(client.getId(), response.clientId());
        assertEquals(availableTestDriveCar.getId(), response.carId());
        assertEquals(TestDriveStatus.CREATED, response.status());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenClientDoesNotExist() {
        CreateTestDriveRequest request = new CreateTestDriveRequest(
                UUID.randomUUID(),
                availableTestDriveCar.getId(),
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(EntityNotFoundException.class,
                () -> testDriveService.createTestDriveRequest(request));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCarDoesNotExist() {
        CreateTestDriveRequest request = new CreateTestDriveRequest(
                client.getId(),
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(EntityNotFoundException.class,
                () -> testDriveService.createTestDriveRequest(request));
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenDateIsInPast() {
        CreateTestDriveRequest request = new CreateTestDriveRequest(
                client.getId(),
                availableTestDriveCar.getId(),
                LocalDateTime.now().minusDays(1)
        );

        assertThrows(DomainValidationException.class,
                () -> testDriveService.createTestDriveRequest(request));
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenCarIsUnavailable() {
        CreateTestDriveRequest request = new CreateTestDriveRequest(
                client.getId(),
                unavailableTestDriveCar.getId(),
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(DomainValidationException.class,
                () -> testDriveService.createTestDriveRequest(request));
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenCarIsNotAvailableForTestDrive() {
        CreateTestDriveRequest request = new CreateTestDriveRequest(
                client.getId(),
                notForTestDriveCar.getId(),
                LocalDateTime.now().plusDays(1)
        );

        assertThrows(DomainValidationException.class,
                () -> testDriveService.createTestDriveRequest(request));
    }

    @Test
    void shouldReturnCarsAvailableForTestDrive() {
        assertEquals(1, testDriveService.getCarsAvailableForTestDrive().size());
        assertEquals(availableTestDriveCar.getId(),
                testDriveService.getCarsAvailableForTestDrive().get(0).id());
    }

    @Test
    void shouldAddCarToTestDriveList() {
        CarResponse response = testDriveService.addCarToTestDriveList(notForTestDriveCar.getId());

        assertTrue(response.availableForTestDrive());

        Car updatedCar = carRepository.findById(notForTestDriveCar.getId()).orElseThrow();
        assertTrue(updatedCar.isAvailableForTestDrive());
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenAddingUnavailableCarToTestDriveList() {
        assertThrows(DomainValidationException.class,
                () -> testDriveService.addCarToTestDriveList(unavailableTestDriveCar.getId()));
    }

    @Test
    void shouldRemoveCarFromTestDriveList() {
        CarResponse response = testDriveService.removeCarFromTestDriveList(availableTestDriveCar.getId());

        assertFalse(response.availableForTestDrive());

        Car updatedCar = carRepository.findById(availableTestDriveCar.getId()).orElseThrow();
        assertFalse(updatedCar.isAvailableForTestDrive());
    }

    @Test
    void shouldApproveCompleteAndCancelTestDriveRequest() {
        TestDriveRequestResponse created = testDriveService.createTestDriveRequest(
                new CreateTestDriveRequest(
                        client.getId(),
                        availableTestDriveCar.getId(),
                        LocalDateTime.now().plusDays(1)
                )
        );

        assertEquals(TestDriveStatus.APPROVED,
                testDriveService.approveTestDriveRequest(created.id()).status());

        assertEquals(TestDriveStatus.COMPLETED,
                testDriveService.completeTestDriveRequest(created.id()).status());
    }

    @Test
    void shouldCancelTestDriveRequest() {
        TestDriveRequestResponse created = testDriveService.createTestDriveRequest(
                new CreateTestDriveRequest(
                        client.getId(),
                        availableTestDriveCar.getId(),
                        LocalDateTime.now().plusDays(1)
                )
        );

        assertEquals(TestDriveStatus.CANCELED,
                testDriveService.cancelTestDriveRequest(created.id()).status());
    }
}