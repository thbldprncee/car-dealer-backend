package org.example;

import org.example.client.StorageServiceClient;
import org.example.config.TraceIdProvider;
import org.example.config.security.CurrentUserService;
import org.example.domain.enums.CarColor;
import org.example.domain.enums.CustomOrderStatus;
import org.example.domain.enums.InStockOrderStatus;
import org.example.domain.model.Client;
import org.example.domain.model.CustomCarOrder;
import org.example.domain.model.InStockCarOrder;
import org.example.domain.model.SalesManager;
import org.example.dto.request.CreateCustomCarOrderRequest;
import org.example.dto.request.CreateInStockCarOrderRequest;
import org.example.dto.response.CustomCarOrderResponse;
import org.example.dto.response.InStockCarOrderResponse;
import org.example.mappers.CustomCarOrderMapper;
import org.example.mappers.InStockCarOrderMapper;
import org.example.repository.ClientRepository;
import org.example.repository.CustomCarOrderRepository;
import org.example.repository.InStockCarOrderRepository;
import org.example.repository.SalesManagerRepository;
import org.example.service.OrderService;
import org.example.service.OutboxEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderCreationSnapshotTest {

    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private final SalesManagerRepository salesManagerRepository = mock(SalesManagerRepository.class);
    private final InStockCarOrderRepository inStockCarOrderRepository = mock(InStockCarOrderRepository.class);
    private final CustomCarOrderRepository customCarOrderRepository = mock(CustomCarOrderRepository.class);
    private final InStockCarOrderMapper inStockCarOrderMapper = mock(InStockCarOrderMapper.class);
    private final CustomCarOrderMapper customCarOrderMapper = mock(CustomCarOrderMapper.class);
    private final StorageServiceClient storageServiceClient = mock(StorageServiceClient.class);

    private final UUID clientId = UUID.randomUUID();
    private final Client client = new Client(clientId, "Ivan", "Petrov", "ivan.petrov@example.com");
    private final SalesManager salesManager = new SalesManager(
            UUID.randomUUID(),
            "Anna",
            "Smirnova",
            "anna.smirnova@example.com"
    );

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                currentUserService,
                mock(TraceIdProvider.class),
                clientRepository,
                salesManagerRepository,
                inStockCarOrderRepository,
                customCarOrderRepository,
                inStockCarOrderMapper,
                customCarOrderMapper,
                mock(OutboxEventService.class),
                storageServiceClient
        );

        when(currentUserService.getCurrentUserId()).thenReturn(clientId);
        when(clientRepository.findByIdAndRemovedFalse(clientId)).thenReturn(Optional.of(client));
        when(salesManagerRepository.findAllByRemovedFalse()).thenReturn(List.of(salesManager));
        when(inStockCarOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(customCarOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(inStockCarOrderMapper.toResponse(any())).thenAnswer(invocation -> toInStockResponse(invocation.getArgument(0)));
        when(customCarOrderMapper.toResponse(any())).thenAnswer(invocation -> toCustomResponse(invocation.getArgument(0)));
    }

    @Test
    void customOrderCreationUsesStorageConfigurationSnapshot() {
        UUID carModelId = UUID.randomUUID();
        UUID configurationId = UUID.randomUUID();
        BigDecimal totalPrice = new BigDecimal("3500000.00");
        when(storageServiceClient.getConfiguration(configurationId)).thenReturn(
                new StorageServiceClient.CarConfigurationSnapshot(
                        configurationId,
                        carModelId,
                        "BMW 320i",
                        totalPrice
                )
        );

        CustomCarOrderResponse response = orderService.createCustomOrder(
                new CreateCustomCarOrderRequest(carModelId, configurationId)
        );

        assertThat(response.carConfigurationId()).isEqualTo(configurationId);
        assertThat(response.carModelDisplayName()).isEqualTo("BMW 320i");
        assertThat(response.totalPrice()).isEqualByComparingTo(totalPrice);
        assertThat(response.status()).isEqualTo(CustomOrderStatus.CREATED);
    }

    @Test
    void inStockOrderCreationUsesStorageCarSnapshot() {
        UUID carId = UUID.randomUUID();
        UUID carModelId = UUID.randomUUID();
        when(storageServiceClient.getCar(carId)).thenReturn(
                new StorageServiceClient.CarSnapshot(
                        carId,
                        carModelId,
                        "BMW 320i",
                        "VIN-BMW-320I-0001",
                        CarColor.BLACK,
                        new BigDecimal("3650000.00")
                )
        );

        InStockCarOrderResponse response = orderService.createInStockOrder(new CreateInStockCarOrderRequest(carId));

        assertThat(response.carId()).isEqualTo(carId);
        assertThat(response.carDisplayName()).isEqualTo("BMW 320i / VIN-BMW-320I-0001");
        assertThat(response.carDisplayName()).doesNotStartWith("Car ");
        assertThat(response.status()).isEqualTo(InStockOrderStatus.CREATED);
    }

    private InStockCarOrderResponse toInStockResponse(InStockCarOrder order) {
        return new InStockCarOrderResponse(
                order.getId(),
                order.getClient().getId(),
                order.getClient().getFullName(),
                order.getSalesManager().getId(),
                order.getSalesManager().getFullName(),
                order.getCarId(),
                order.getCarDisplayName(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    private CustomCarOrderResponse toCustomResponse(CustomCarOrder order) {
        return new CustomCarOrderResponse(
                order.getId(),
                order.getClient().getId(),
                order.getClient().getFullName(),
                order.getSalesManager().getId(),
                order.getSalesManager().getFullName(),
                order.getCarModelId(),
                order.getCarModelDisplayName(),
                order.getCarConfigurationId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
