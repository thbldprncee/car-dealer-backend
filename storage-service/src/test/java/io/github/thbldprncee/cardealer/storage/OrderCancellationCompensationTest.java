package io.github.thbldprncee.cardealer.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.thbldprncee.cardealer.events.ApprovalResult;
import io.github.thbldprncee.cardealer.events.OrderApprovalResultEvent;
import io.github.thbldprncee.cardealer.events.OrderCanceledEvent;
import io.github.thbldprncee.cardealer.events.OrderSentForApprovalEvent;
import io.github.thbldprncee.cardealer.events.OrderType;
import io.github.thbldprncee.cardealer.storage.domain.enums.AssemblyOrderStatus;
import io.github.thbldprncee.cardealer.storage.domain.model.AssemblyOrder;
import io.github.thbldprncee.cardealer.storage.domain.model.Car;
import io.github.thbldprncee.cardealer.storage.repository.AssemblyOrderRepository;
import io.github.thbldprncee.cardealer.storage.repository.CarConfigurationRepository;
import io.github.thbldprncee.cardealer.storage.repository.CarModelRepository;
import io.github.thbldprncee.cardealer.storage.repository.CarRepository;
import io.github.thbldprncee.cardealer.storage.repository.ComponentOptionRepository;
import io.github.thbldprncee.cardealer.storage.repository.ProcessedEventRepository;
import io.github.thbldprncee.cardealer.storage.service.OrderCanceledListener;
import io.github.thbldprncee.cardealer.storage.service.OrderSentForApprovalListener;
import io.github.thbldprncee.cardealer.storage.service.OutboxEventService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The compensating half of the approval saga: canceled orders give their car back. */
class OrderCancellationCompensationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final ProcessedEventRepository processedEvents = mock(ProcessedEventRepository.class);
    private final AssemblyOrderRepository assemblyOrders = mock(AssemblyOrderRepository.class);
    private final CarRepository cars = mock(CarRepository.class);
    private final OutboxEventService outbox = mock(OutboxEventService.class);

    private final OrderCanceledListener canceledListener =
            new OrderCanceledListener(objectMapper, processedEvents, assemblyOrders, cars);

    private final UUID orderId = UUID.randomUUID();
    private final UUID carId = UUID.randomUUID();

    @Test
    void releasesTheCarReservedForACanceledOrder() throws Exception {
        AssemblyOrder assembly = assembly(OrderType.IN_STOCK);
        assembly.markAsAssembled();
        Car car = mock(Car.class);
        when(assemblyOrders.findBySourceOrderIdAndRemovedFalse(orderId)).thenReturn(Optional.of(assembly));
        when(cars.findByIdAndRemovedFalse(carId)).thenReturn(Optional.of(car));

        canceledListener.handle(canceled(OrderType.IN_STOCK));

        verify(car).markAsAvailable();
        verify(cars).save(car);
        assertThat(assembly.getStatus()).isEqualTo(AssemblyOrderStatus.CANCELED);
    }

    @Test
    void keepsTheCarWhenTheReservationHadFailed() throws Exception {
        AssemblyOrder assembly = assembly(OrderType.IN_STOCK);
        assembly.markAsFailed("Car is not available");
        when(assemblyOrders.findBySourceOrderIdAndRemovedFalse(orderId)).thenReturn(Optional.of(assembly));

        canceledListener.handle(canceled(OrderType.IN_STOCK));

        verify(cars, never()).findByIdAndRemovedFalse(any());
        assertThat(assembly.getStatus()).isEqualTo(AssemblyOrderStatus.CANCELED);
    }

    @Test
    void ignoresARedeliveredCancellation() throws Exception {
        String payload = canceled(OrderType.IN_STOCK);
        OrderCanceledEvent event = objectMapper.readValue(payload, OrderCanceledEvent.class);
        when(processedEvents.existsById(event.eventId())).thenReturn(true);

        canceledListener.handle(payload);

        verify(assemblyOrders, never()).findBySourceOrderIdAndRemovedFalse(any());
    }

    @Test
    void approvalRequestArrivingAfterTheCancellationIsRejected() throws Exception {
        when(assemblyOrders.findBySourceOrderIdAndRemovedFalse(orderId)).thenReturn(Optional.empty());
        canceledListener.handle(canceled(OrderType.IN_STOCK));

        ArgumentCaptor<AssemblyOrder> recorded = ArgumentCaptor.forClass(AssemblyOrder.class);
        verify(assemblyOrders).save(recorded.capture());
        assertThat(recorded.getValue().isCanceled()).isTrue();

        when(assemblyOrders.findBySourceOrderIdAndRemovedFalse(orderId)).thenReturn(Optional.of(recorded.getValue()));
        OrderSentForApprovalListener approvalListener = new OrderSentForApprovalListener(
                objectMapper, processedEvents, assemblyOrders, cars, mock(CarModelRepository.class),
                mock(CarConfigurationRepository.class), mock(ComponentOptionRepository.class), outbox);
        ReflectionTestUtils.setField(approvalListener, "orderApprovalResultTopic", "order.approval-result");

        approvalListener.handle(objectMapper.writeValueAsString(new OrderSentForApprovalEvent(
                UUID.randomUUID(), orderId, OrderType.IN_STOCK, carId, null, null, List.of(), "trace", Instant.now())));

        verify(cars, never()).save(any());
        ArgumentCaptor<Object> result = ArgumentCaptor.forClass(Object.class);
        verify(outbox).saveEvent(eq(orderId), anyString(), anyString(), eq("order.approval-result"), result.capture(), any());
        OrderApprovalResultEvent resultEvent = (OrderApprovalResultEvent) result.getValue();
        assertThat(resultEvent.result()).isEqualTo(ApprovalResult.REJECTED);
        assertThat(resultEvent.reason()).isEqualTo("Order was canceled");
    }

    private AssemblyOrder assembly(OrderType type) {
        return new AssemblyOrder(UUID.randomUUID(), orderId, type, carId, null, Set.of(), "trace");
    }

    private String canceled(OrderType type) throws Exception {
        return objectMapper.writeValueAsString(
                new OrderCanceledEvent(UUID.randomUUID(), orderId, type, carId, "trace", Instant.now()));
    }
}
