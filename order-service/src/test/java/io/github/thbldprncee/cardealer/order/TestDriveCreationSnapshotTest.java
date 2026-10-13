package io.github.thbldprncee.cardealer.order;

import io.github.thbldprncee.cardealer.order.client.StorageServiceClient;
import io.github.thbldprncee.cardealer.order.config.security.CurrentUserService;
import io.github.thbldprncee.cardealer.order.domain.enums.CarColor;
import io.github.thbldprncee.cardealer.order.domain.enums.TestDriveStatus;
import io.github.thbldprncee.cardealer.order.domain.model.Client;
import io.github.thbldprncee.cardealer.order.domain.model.TestDriveRequest;
import io.github.thbldprncee.cardealer.order.dto.request.CreateTestDriveRequest;
import io.github.thbldprncee.cardealer.order.dto.response.TestDriveRequestResponse;
import io.github.thbldprncee.cardealer.order.mappers.TestDriveRequestMapper;
import io.github.thbldprncee.cardealer.order.repository.ClientRepository;
import io.github.thbldprncee.cardealer.order.repository.TestDriveRequestRepository;
import io.github.thbldprncee.cardealer.order.service.TestDriveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestDriveCreationSnapshotTest {

    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private final TestDriveRequestRepository testDriveRequestRepository = mock(TestDriveRequestRepository.class);
    private final TestDriveRequestMapper testDriveRequestMapper = mock(TestDriveRequestMapper.class);
    private final StorageServiceClient storageServiceClient = mock(StorageServiceClient.class);

    private final UUID clientId = UUID.randomUUID();
    private final Client client = new Client(clientId, "Ivan", "Petrov", "ivan.petrov@example.com");

    private TestDriveService testDriveService;

    @BeforeEach
    void setUp() {
        testDriveService = new TestDriveService(
                currentUserService,
                clientRepository,
                testDriveRequestRepository,
                testDriveRequestMapper,
                storageServiceClient
        );

        when(currentUserService.getCurrentUserId()).thenReturn(clientId);
        when(clientRepository.findByIdAndRemovedFalse(clientId)).thenReturn(Optional.of(client));
        when(testDriveRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(testDriveRequestMapper.toResponse(any())).thenAnswer(invocation -> toResponse(invocation.getArgument(0)));
    }

    @Test
    void testDriveCreationUsesStorageCarSnapshot() {
        UUID carId = UUID.randomUUID();
        UUID carModelId = UUID.randomUUID();
        when(storageServiceClient.getCar(carId)).thenReturn(
                new StorageServiceClient.CarSnapshot(
                        carId,
                        carModelId,
                        "BMW 320i",
                        "VIN-BMW-320I-0002",
                        CarColor.WHITE,
                        new BigDecimal("3620000.00")
                )
        );

        TestDriveRequestResponse response = testDriveService.createTestDriveRequest(
                new CreateTestDriveRequest(carId, LocalDateTime.now().plusDays(1))
        );

        assertThat(response.carId()).isEqualTo(carId);
        assertThat(response.carDisplayName()).isEqualTo("BMW 320i / VIN-BMW-320I-0002");
        assertThat(response.carDisplayName()).doesNotStartWith("Car ");
        assertThat(response.status()).isEqualTo(TestDriveStatus.CREATED);
    }

    private TestDriveRequestResponse toResponse(TestDriveRequest request) {
        return new TestDriveRequestResponse(
                request.getId(),
                request.getClient().getId(),
                request.getClient().getFullName(),
                request.getCarId(),
                request.getCarDisplayName(),
                request.getStartDateTime(),
                request.getStatus()
        );
    }
}
