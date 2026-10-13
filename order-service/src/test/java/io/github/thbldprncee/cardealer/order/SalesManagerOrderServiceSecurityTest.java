package io.github.thbldprncee.cardealer.order;

import io.github.thbldprncee.cardealer.order.presentation.OrderController;
import io.github.thbldprncee.cardealer.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SalesManagerOrderServiceSecurityTest {

    @Test
    void salesManagerAndAdminCanRunDeliveryAndCompletionTransitions() throws NoSuchMethodException {
        assertManagerOrAdmin(OrderService.class, "moveCustomOrderToWaitingForDelivery");
        assertManagerOrAdmin(OrderService.class, "markCustomOrderReadyForDelivery");
        assertManagerOrAdmin(OrderService.class, "completeInStockOrder");
        assertManagerOrAdmin(OrderService.class, "completeCustomOrder");

        assertManagerOrAdmin(OrderController.class, "moveCustomOrderToWaitingForDelivery");
        assertManagerOrAdmin(OrderController.class, "markCustomOrderReadyForDelivery");
        assertManagerOrAdmin(OrderController.class, "completeInStockOrder");
        assertManagerOrAdmin(OrderController.class, "completeCustomOrder");
    }

    private void assertManagerOrAdmin(Class<?> type, String methodName) throws NoSuchMethodException {
        PreAuthorize preAuthorize = type
                .getMethod(methodName, UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('MANAGER', 'ADMIN')");
    }
}
