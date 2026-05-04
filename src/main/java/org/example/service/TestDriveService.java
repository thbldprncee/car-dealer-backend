package org.example.service;

import lombok.RequiredArgsConstructor;
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
import org.example.repository.ClientRepository;
import org.example.repository.TestDriveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestDriveService {

    private final ClientRepository clientRepository;
    private final CarRepository carRepository;
    private final TestDriveRequestRepository testDriveRequestRepository;

    private final TestDriveRequestMapper testDriveRequestMapper = TestDriveRequestMapper.INSTANCE;
    private final CarMapper carMapper = CarMapper.INSTANCE;

    @Transactional
    public TestDriveRequestResponse createTestDriveRequest(CreateTestDriveRequest request) {
        validateCreateRequest(request);

        Client client = clientRepository.findById(request.clientId())
                .filter(existingClient -> !existingClient.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Client", request.clientId()));

        Car car = carRepository.findById(request.carId())
                .filter(existingCar -> !existingCar.isRemoved())
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
                .filter(request -> !request.isRemoved())
                .map(testDriveRequestMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest", id));
    }

    public List<TestDriveRequestResponse> getAllTestDriveRequests() {
        return testDriveRequestRepository.findAllByRemovedFalse().stream()
                .map(testDriveRequestMapper::toResponse)
                .toList();
    }

    public List<TestDriveRequestResponse> getTestDriveRequestsByStatus(TestDriveStatus status) {
        if (status == null) {
            throw new DomainValidationException("Test drive status must not be null");
        }

        return testDriveRequestRepository.findAllByStatusAndRemovedFalse(status).stream()
                .map(testDriveRequestMapper::toResponse)
                .toList();
    }

    public List<CarResponse> getCarsAvailableForTestDrive() {
        return carRepository.findAllByAvailableForTestDriveTrueAndRemovedFalse().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    @Transactional
    public CarResponse addCarToTestDriveList(UUID carId) {
        Car car = carRepository.findById(carId)
                .filter(existingCar -> !existingCar.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Car", carId));

        if (!car.isAvailable()) {
            throw new DomainValidationException("Car is not available");
        }

        car.enableTestDrive();

        return carMapper.toResponse(carRepository.save(car));
    }

    @Transactional
    public CarResponse removeCarFromTestDriveList(UUID carId) {
        Car car = carRepository.findById(carId)
                .filter(existingCar -> !existingCar.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Car", carId));

        car.disableTestDrive();

        return carMapper.toResponse(carRepository.save(car));
    }

    @Transactional
    public TestDriveRequestResponse approveTestDriveRequest(UUID requestId) {
        TestDriveRequest request = findActiveTestDriveRequest(requestId);
        request.approve();

        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    @Transactional
    public TestDriveRequestResponse completeTestDriveRequest(UUID requestId) {
        TestDriveRequest request = findActiveTestDriveRequest(requestId);
        request.complete();

        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    @Transactional
    public TestDriveRequestResponse cancelTestDriveRequest(UUID requestId) {
        TestDriveRequest request = findActiveTestDriveRequest(requestId);
        request.cancel();

        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    @Transactional
    public void deleteTestDriveRequest(UUID requestId) {
        TestDriveRequest request = findActiveTestDriveRequest(requestId);
        request.markAsRemoved();

        testDriveRequestRepository.save(request);
    }

    private TestDriveRequest findActiveTestDriveRequest(UUID requestId) {
        return testDriveRequestRepository.findById(requestId)
                .filter(request -> !request.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest", requestId));
    }

    private void validateCreateRequest(CreateTestDriveRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create test drive request must not be null");
        }

        if (request.clientId() == null) {
            throw new DomainValidationException("Client id must not be null");
        }

        if (request.carId() == null) {
            throw new DomainValidationException("Car id must not be null");
        }

        if (request.startDateTime() == null) {
            throw new DomainValidationException("Test drive start date time must not be null");
        }
    }
}