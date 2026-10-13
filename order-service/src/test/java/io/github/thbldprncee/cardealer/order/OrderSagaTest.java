package io.github.thbldprncee.cardealer.order;

import io.github.thbldprncee.cardealer.events.OrderCanceledEvent;
import io.github.thbldprncee.cardealer.events.OrderSentForApprovalEvent;
import io.github.thbldprncee.cardealer.events.OrderType;
import io.github.thbldprncee.cardealer.order.client.StorageServiceClient;
import io.github.thbldprncee.cardealer.order.config.TraceIdProvider;
import io.github.thbldprncee.cardealer.order.config.security.CurrentUserService;
import io.github.thbldprncee.cardealer.order.domain.enums.CustomOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.enums.InStockOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.exceptions.DomainValidationException;
import io.github.thbldprncee.cardealer.order.domain.model.Client;
import io.github.thbldprncee.cardealer.order.domain.model.CustomCarOrder;
import io.github.thbldprncee.cardealer.order.domain.model.InStockCarOrder;
import io.github.thbldprncee.cardealer.order.domain.model.SalesManager;
import io.github.thbldprncee.cardealer.order.dto.request.CreateCustomCarOrderRequest;
import io.github.thbldprncee.cardealer.order.mappers.CustomCarOrderMapper;
import io.github.thbldprncee.cardealer.order.mappers.InStockCarOrderMapper;
import io.github.thbldprncee.cardealer.order.repository.ClientRepository;
import io.github.thbldprncee.cardealer.order.repository.CustomCarOrderRepository;
import io.github.thbldprncee.cardealer.order.repository.InStockCarOrderRepository;
import io.github.thbldprncee.cardealer.order.repository.SalesManagerRepository;
import io.github.thbldprncee.cardealer.order.service.OrderService;
import io.github.thbldprncee.cardealer.order.service.OutboxEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Which saga events order-service writes to the outbox at each step of an order's life. */
class OrderSagaTest {

    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private final SalesManagerRepository salesManagerRepository = mock(SalesManagerRepository.class);
    private final InStockCarOrderRepository inStockCarOrderRepository = mock(InStockCarOrderRepository.class);
    private final CustomCarOrderRepository customCarOrderRepository = mock(CustomCarOrderRepository.class);
    private final OutboxEventService outboxEventService = mock(OutboxEventService.class);
    private final StorageServiceClient storageServiceClient = mock(StorageServiceClient.class);

    private final UUID clientId = UUID.randomUUID();
    private final Client client = new Client(clientId, "Ivan", "Petrov", "ivan.petrov@example.com");
    private final SalesManager manager = new SalesManager(UUID.randomUUID(), "Anna", "Smirnova", "anna@example.com");

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
                mock(InStockCarOrderMapper.class),
                mock(CustomCarOrderMapper.class),
                outboxEventService,
                storageServiceClient
        );
        ReflectionTestUtils.setField(orderService, "orderSentForApprovalTopic", "order.sent-for-approval");
        ReflectionTestUtils.setField(orderService, "orderCanceledTopic", "order.canceled");

        when(currentUserService.getCurrentUserId()).thenReturn(clientId);
        when(clientRepository.findByIdAndRemovedFalse(clientId)).thenReturn(Optional.of(client));
        when(salesManagerRepository.findAllByRemovedFalse()).thenReturn(List.of(manager));
        when(inStockCarOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(customCarOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void customOrderIsSentToTheWarehouseWhenCreated() {
        UUID modelId = UUID.randomUUID();
        UUID configurationId = UUID.randomUUID();
        when(storageServiceClient.getConfiguration(configurationId)).thenReturn(
                new StorageServiceClient.CarConfigurationSnapshot(configurationId, modelId, "BMW 320i", BigDecimal.TEN));

        orderService.createCustomOrder(new CreateCustomCarOrderRequest(modelId, configurationId));

        OrderSentForApprovalEvent event = captured(OrderSentForApprovalEvent.class, "order.sent-for-approval");
        assertThat(event.orderType()).isEqualTo(OrderType.CUSTOM);
        assertThat(event.configurationId()).isEqualTo(configurationId);
        assertThat(event.carModelId()).isEqualTo(modelId);
    }

    @Test
    void customOrderRejectsConfigurationOfAnotherModel() {
        UUID configurationId = UUID.randomUUID();
        when(storageServiceClient.getConfiguration(configurationId)).thenReturn(
                new StorageServiceClient.CarConfigurationSnapshot(configurationId, UUID.randomUUID(), "Audi A4", BigDecimal.TEN));

        assertThatThrownBy(() -> orderService.createCustomOrder(
                new CreateCustomCarOrderRequest(UUID.randomUUID(), configurationId)))
                .isInstanceOf(DomainValidationException.class)
                .hasMessageContaining("another car model");
        verify(outboxEventService, never()).saveEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void payingForCustomOrderSendsNothingToTheWarehouse() {
        CustomCarOrder order = stored(CustomOrderStatus.WAITING_FOR_PAYMENT);

        orderService.payCustomOrder(order.getId());

        assertThat(order.getStatus()).isEqualTo(CustomOrderStatus.PAID);
        verify(outboxEventService, never()).saveEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void payingForInStockOrderAsksTheWarehouseToReserveTheCar() {
        InStockCarOrder order = stored(InStockOrderStatus.WAITING_FOR_PAYMENT);

        orderService.payInStockOrder(order.getId());

        OrderSentForApprovalEvent event = captured(OrderSentForApprovalEvent.class, "order.sent-for-approval");
        assertThat(event.orderType()).isEqualTo(OrderType.IN_STOCK);
        assertThat(event.carId()).isEqualTo(order.getCarId());
    }

    @ParameterizedTest
    @EnumSource(value = InStockOrderStatus.class, names = {"PAID", "READY_FOR_DELIVERY"})
    void cancelingInStockOrderKnownToTheWarehouseReleasesTheCar(InStockOrderStatus status) {
        InStockCarOrder order = stored(status);

        orderService.cancelInStockOrder(order.getId());

        OrderCanceledEvent event = captured(OrderCanceledEvent.class, "order.canceled");
        assertThat(event.orderId()).isEqualTo(order.getId());
        assertThat(event.carId()).isEqualTo(order.getCarId());
        assertThat(order.getStatus()).isEqualTo(InStockOrderStatus.CANCELED);
    }

    @ParameterizedTest
    @EnumSource(value = InStockOrderStatus.class, names = {"CREATED", "APPROVED_BY_MANAGER", "WAITING_FOR_PAYMENT"})
    void cancelingInStockOrderBeforePaymentNeedsNoCompensation(InStockOrderStatus status) {
        InStockCarOrder order = stored(status);

        orderService.cancelInStockOrder(order.getId());

        verify(outboxEventService, never()).saveEvent(any(), any(), any(), any(), any(), any());
    }

    @Test
    void cancelingCustomOrderNotifiesTheWarehouse() {
        CustomCarOrder order = stored(CustomOrderStatus.APPROVED_BY_WAREHOUSE);

        orderService.cancelCustomOrder(order.getId());

        assertThat(captured(OrderCanceledEvent.class, "order.canceled").orderType()).isEqualTo(OrderType.CUSTOM);
    }

    private <T> T captured(Class<T> type, String topic) {
        ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
        verify(outboxEventService).saveEvent(any(), anyString(), eq(type.getSimpleName()), eq(topic), event.capture(), any());
        return type.cast(event.getValue());
    }

    private InStockCarOrder stored(InStockOrderStatus status) {
        InStockCarOrder order = new InStockCarOrder(UUID.randomUUID(), client, manager, UUID.randomUUID(), "BMW 320i / VIN", status);
        when(inStockCarOrderRepository.findByIdAndRemovedFalse(order.getId())).thenReturn(Optional.of(order));
        return order;
    }

    private CustomCarOrder stored(CustomOrderStatus status) {
        CustomCarOrder order = new CustomCarOrder(UUID.randomUUID(), client, manager, UUID.randomUUID(), "BMW 320i",
                UUID.randomUUID(), BigDecimal.TEN, status);
        when(customCarOrderRepository.findByIdAndRemovedFalse(order.getId())).thenReturn(Optional.of(order));
        return order;
    }
}
