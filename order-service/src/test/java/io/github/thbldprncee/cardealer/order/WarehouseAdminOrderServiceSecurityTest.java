package io.github.thbldprncee.cardealer.order;

import io.github.thbldprncee.cardealer.order.presentation.OrderController;
import io.github.thbldprncee.cardealer.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarehouseAdminOrderServiceSecurityTest {

    @Test
    void warehouseAdminCanApproveCustomOrderByWarehouse() throws NoSuchMethodException {
        assertWarehouseOrAdmin(OrderService.class, "approveCustomOrderByWarehouse");
        assertWarehouseOrAdmin(OrderController.class, "approveCustomOrderByWarehouse");
    }

    private void assertAdminOnly(Class<?> type, String methodName) throws NoSuchMethodException {
        PreAuthorize preAuthorize = type
                .getMethod(methodName, UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasRole('ADMIN')");
        assertThat(preAuthorize.value()).doesNotContain("WAREHOUSE_ADMIN");
    }

    private void assertWarehouseOrAdmin(Class<?> type, String methodName) throws NoSuchMethodException {
        PreAuthorize preAuthorize = type
                .getMethod(methodName, UUID.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')");
    }
}
