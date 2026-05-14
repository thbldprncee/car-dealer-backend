package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.domain.enums.CustomOrderStatus;
import org.example.domain.model.Client;
import org.example.domain.model.CustomCarOrder;
import org.example.domain.model.SalesManager;
import org.example.events.ApprovalResult;
import org.example.events.OrderApprovalResultEvent;
import org.example.events.OrderType;
import org.example.repository.CustomCarOrderRepository;
import org.example.repository.InStockCarOrderRepository;
import org.example.repository.ProcessedEventRepository;
import org.example.service.OrderApprovalResultListener;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderApprovalResultListenerCustomTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final ProcessedEventRepository processedEventRepository = mock(ProcessedEventRepository.class);
    private final InStockCarOrderRepository inStockCarOrderRepository = mock(InStockCarOrderRepository.class);
    private final CustomCarOrderRepository customCarOrderRepository = mock(CustomCarOrderRepository.class);
    private final OrderApprovalResultListener listener = new OrderApprovalResultListener(
            objectMapper,
            processedEventRepository,
            inStockCarOrderRepository,
            customCarOrderRepository
    );

    @Test
    void approvedCustomResultMovesCreatedOrderToApprovedByWarehouse() throws Exception {
        CustomCarOrder order = customOrder(CustomOrderStatus.CREATED);
        when(processedEventRepository.existsById(any())).thenReturn(false);
        when(customCarOrderRepository.findByIdAndRemovedFalse(order.getId())).thenReturn(Optional.of(order));

        listener.handle(payload(order.getId()));

        assertThat(order.getStatus()).isEqualTo(CustomOrderStatus.APPROVED_BY_WAREHOUSE);
    }

    @Test
    void repeatedApprovedCustomResultDoesNotBreakAlreadyApprovedOrder() throws Exception {
        CustomCarOrder order = customOrder(CustomOrderStatus.CREATED);
        when(processedEventRepository.existsById(any())).thenReturn(false);
        when(customCarOrderRepository.findByIdAndRemovedFalse(order.getId())).thenReturn(Optional.of(order));

        listener.handle(payload(order.getId()));

        assertThatCode(() -> listener.handle(payload(order.getId()))).doesNotThrowAnyException();
        assertThat(order.getStatus()).isEqualTo(CustomOrderStatus.APPROVED_BY_WAREHOUSE);
    }

    @Test
    void approvedCustomResultDoesNotMoveLaterOrderBackOrThrow() {
        CustomCarOrder order = customOrder(CustomOrderStatus.PAID);
        when(processedEventRepository.existsById(any())).thenReturn(false);
        when(customCarOrderRepository.findByIdAndRemovedFalse(order.getId())).thenReturn(Optional.of(order));

        assertThatCode(() -> listener.handle(payload(order.getId()))).doesNotThrowAnyException();
        assertThat(order.getStatus()).isEqualTo(CustomOrderStatus.PAID);
    }

    private String payload(UUID orderId) throws Exception {
        return objectMapper.writeValueAsString(new OrderApprovalResultEvent(
                UUID.randomUUID(),
                orderId,
                OrderType.CUSTOM,
                ApprovalResult.APPROVED,
                null,
                UUID.randomUUID().toString(),
                Instant.now()
        ));
    }

    private CustomCarOrder customOrder(CustomOrderStatus status) {
        return new CustomCarOrder(
                UUID.randomUUID(),
                mock(Client.class),
                mock(SalesManager.class),
                UUID.randomUUID(),
                "Car model",
                UUID.randomUUID(),
                BigDecimal.ZERO,
                status
        );
    }
}
