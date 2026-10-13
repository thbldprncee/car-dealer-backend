package io.github.thbldprncee.cardealer.order;

import io.github.thbldprncee.cardealer.order.presentation.OrderController;
import io.github.thbldprncee.cardealer.order.service.OrderService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Warehouse decisions belong to storage-service and arrive as Kafka events; order-service must not
 * offer a REST shortcut that would skip the stock check.
 */
class WarehouseDecisionsTest {

    @Test
    void warehouseTransitionsAreNotExposedAsEndpoints() {
        assertThat(methodNames(OrderController.class))
                .doesNotContain("approveCustomOrderByWarehouse", "markInStockOrderReadyForDelivery");
        assertThat(methodNames(OrderService.class))
                .doesNotContain("approveCustomOrderByWarehouse", "markInStockOrderReadyForDelivery");
    }

    private static String[] methodNames(Class<?> type) {
        return Arrays.stream(type.getMethods()).map(Method::getName).toArray(String[]::new);
    }
}
