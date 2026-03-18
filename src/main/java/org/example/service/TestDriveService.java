package org.example.service;

import org.example.domain.enums.TestDriveStatus;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.model.Car;
import org.example.domain.model.Client;
import org.example.domain.model.TestDriveRequest;
import org.example.dto.request.CreateTestDriveRequest;
import org.example.dto.response.CarResponse;
import org.example.dto.response.TestDriveRequestResponse;
import org.example.mappers.CarMapper;
import org.example.mappers.TestDriveRequestMapper;
import org.example.repository.CarRepository;
import org.example.repository.TestDriveRequestRepository;
import org.example.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TestDriveService {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final TestDriveRequestRepository testDriveRequestRepository;
    private final TestDriveRequestMapper testDriveRequestMapper;
    private final CarMapper carMapper;

    public TestDriveService(
            UserRepository userRepository,
            CarRepository carRepository,
            TestDriveRequestRepository testDriveRequestRepository
    ) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.testDriveRequestRepository = testDriveRequestRepository;
        this.testDriveRequestMapper = TestDriveRequestMapper.INSTANCE;
        this.carMapper = CarMapper.INSTANCE;
    }

    public TestDriveRequestResponse createTestDriveRequest(CreateTestDriveRequest request) {
        validateCreateRequest(request);

        Client client = userRepository.findById(request.clientId())
                .filter(Client.class::isInstance)
                .map(Client.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Client", request.clientId()));

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new EntityNotFoundException("Car", request.carId()));

        if (request.startDateTime().isBefore(LocalDateTime.now())) {
            throw new DomainValidationException("Test drive start date time must be in the future");
        }

        TestDriveRequest testDriveRequest = new TestDriveRequest(
                UUID.randomUUID(),
                client,
                car,
                request.startDateTime(),
                TestDriveStatus.CREATED
        );

        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(testDriveRequest));
    }

    public TestDriveRequestResponse getTestDriveRequestById(UUID id) {
        return testDriveRequestRepository.findById(id)
                .map(testDriveRequestMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest", id));
    }

    public List<TestDriveRequestResponse> getAllTestDriveRequests() {
        return testDriveRequestRepository.findAll().stream()
                .map(testDriveRequestMapper::toResponse)
                .toList();
    }

    public List<CarResponse> getCarsAvailableForTestDrive() {
        return carRepository.findCarsAvailableForTestDrive().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public CarResponse addCarToTestDriveList(UUID carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car", carId));

        if (!car.isAvailable()) {
            throw new DomainValidationException("Car is not available");
        }

        car.enableTestDrive();
        return carMapper.toResponse(carRepository.save(car));
    }

    public CarResponse removeCarFromTestDriveList(UUID carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car", carId));

        car.disableTestDrive();
        return carMapper.toResponse(carRepository.save(car));
    }

    public TestDriveRequestResponse approveTestDriveRequest(UUID requestId) {
        TestDriveRequest request = testDriveRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest", requestId));

        request.approve();
        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    public TestDriveRequestResponse completeTestDriveRequest(UUID requestId) {
        TestDriveRequest request = testDriveRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest", requestId));

        request.complete();
        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    public TestDriveRequestResponse cancelTestDriveRequest(UUID requestId) {
        TestDriveRequest request = testDriveRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest", requestId));

        request.cancel();
        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    private void validateCreateRequest(CreateTestDriveRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create test drive request must not be null");
        }
    }
}