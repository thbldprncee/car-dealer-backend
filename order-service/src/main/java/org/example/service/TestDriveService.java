package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.client.StorageServiceClient;
import org.example.config.security.CurrentUserService;
import org.example.domain.enums.TestDriveStatus;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.model.Client;
import org.example.domain.model.TestDriveRequest;
import org.example.dto.request.CreateTestDriveRequest;
import org.example.dto.response.TestDriveRequestResponse;
import org.example.mappers.TestDriveRequestMapper;
import org.example.repository.ClientRepository;
import org.example.repository.TestDriveRequestRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestDriveService {

    private final CurrentUserService currentUserService;

    private final ClientRepository clientRepository;
    private final TestDriveRequestRepository testDriveRequestRepository;

    private final TestDriveRequestMapper testDriveRequestMapper;
    private final StorageServiceClient storageServiceClient;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TestDriveRequestResponse createTestDriveRequest(CreateTestDriveRequest request) {
        validateCreateRequest(request);

        UUID currentUserId = currentUserService.getCurrentUserId();

        Client client = clientRepository.findByIdAndRemovedFalse(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Client", currentUserId));

        if (request.startDateTime().isBefore(LocalDateTime.now())) {
            throw new DomainValidationException("Test drive start date time must be in the future");
        }

        StorageServiceClient.CarSnapshot car = storageServiceClient.getCar(request.carId());

        TestDriveRequest testDriveRequest = new TestDriveRequest(
                UUID.randomUUID(),
                client,
                request.carId(),
                car.displayName(),
                request.startDateTime(),
                TestDriveStatus.CREATED
        );

        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(testDriveRequest));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityService.isTestDriveRequestOwner(#id)")
    public TestDriveRequestResponse getTestDriveRequestById(UUID id) {
        return testDriveRequestRepository.findByIdAndRemovedFalse(id)
                .map(testDriveRequestMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest", id));
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<TestDriveRequestResponse> getAllTestDriveRequests() {
        if (!currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            UUID currentUserId = currentUserService.getCurrentUserId();
            return testDriveRequestRepository.findAllByClientIdAndRemovedFalse(currentUserId).stream()
                    .map(testDriveRequestMapper::toResponse)
                    .toList();
        }

        return testDriveRequestRepository.findAllByRemovedFalse().stream()
                .map(testDriveRequestMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<TestDriveRequestResponse> getTestDriveRequestsByStatus(TestDriveStatus status) {
        if (status == null) {
            throw new DomainValidationException("Test drive status must not be null");
        }

        if (!currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            UUID currentUserId = currentUserService.getCurrentUserId();
            return testDriveRequestRepository.findAllByClientIdAndStatusAndRemovedFalse(currentUserId, status).stream()
                    .map(testDriveRequestMapper::toResponse)
                    .toList();
        }

        return testDriveRequestRepository.findAllByStatusAndRemovedFalse(status).stream()
                .map(testDriveRequestMapper::toResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveRequestResponse approveTestDriveRequest(UUID requestId) {
        TestDriveRequest request = findActiveTestDriveRequest(requestId);
        request.approve();

        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveRequestResponse completeTestDriveRequest(UUID requestId) {
        TestDriveRequest request = findActiveTestDriveRequest(requestId);
        request.complete();

        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityService.isTestDriveRequestOwner(#requestId)")
    public TestDriveRequestResponse cancelTestDriveRequest(UUID requestId) {
        TestDriveRequest request = findActiveTestDriveRequest(requestId);
        request.cancel();

        return testDriveRequestMapper.toResponse(testDriveRequestRepository.save(request));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTestDriveRequest(UUID requestId) {
        TestDriveRequest request = findActiveTestDriveRequest(requestId);
        request.markAsRemoved();

        testDriveRequestRepository.save(request);
    }

    private TestDriveRequest findActiveTestDriveRequest(UUID requestId) {
        return testDriveRequestRepository.findByIdAndRemovedFalse(requestId)
                .orElseThrow(() -> new EntityNotFoundException("TestDriveRequest", requestId));
    }

    private void validateCreateRequest(CreateTestDriveRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create test drive request must not be null");
        }

        if (request.carId() == null) {
            throw new DomainValidationException("Car id must not be null");
        }

        if (request.startDateTime() == null) {
            throw new DomainValidationException("Test drive start date time must not be null");
        }
    }
}
