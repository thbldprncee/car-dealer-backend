package io.github.thbldprncee.cardealer.order;

import io.github.thbldprncee.cardealer.order.domain.enums.CustomOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.enums.InStockOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.model.Client;
import io.github.thbldprncee.cardealer.order.domain.model.CustomCarOrder;
import io.github.thbldprncee.cardealer.order.domain.model.InStockCarOrder;
import io.github.thbldprncee.cardealer.order.domain.model.SalesManager;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WarehouseApprovalIdempotencyTest {

    @Test
    void repeatedApprovedResultDoesNotBreakInStockOrder() {
        InStockCarOrder order = inStockOrder(InStockOrderStatus.PAID);

        order.markReadyForDeliveryAfterWarehouseApproval();
        order.markReadyForDeliveryAfterWarehouseApproval();

        assertThat(order.getStatus()).isEqualTo(InStockOrderStatus.READY_FOR_DELIVERY);
    }

    @Test
    void repeatedRejectedResultDoesNotBreakInStockOrder() {
        InStockCarOrder order = inStockOrder(InStockOrderStatus.PAID);

        order.cancelAfterWarehouseRejection("missing car");
        order.cancelAfterWarehouseRejection("missing car");

        assertThat(order.getStatus()).isEqualTo(InStockOrderStatus.CANCELED);
    }

    @Test
    void repeatedApprovedResultApprovesCreatedCustomOrderOnce() {
        CustomCarOrder order = customOrder(CustomOrderStatus.CREATED);

        order.approveByWarehouseAfterApprovalResult();
        order.approveByWarehouseAfterApprovalResult();

        assertThat(order.getStatus()).isEqualTo(CustomOrderStatus.APPROVED_BY_WAREHOUSE);
    }

    @Test
    void approvedResultDoesNotMoveCustomOrderBackFromLaterStatus() {
        CustomCarOrder order = customOrder(CustomOrderStatus.PAID);

        order.approveByWarehouseAfterApprovalResult();

        assertThat(order.getStatus()).isEqualTo(CustomOrderStatus.PAID);
    }

    @Test
    void repeatedRejectedResultDoesNotBreakCustomOrder() {
        CustomCarOrder order = customOrder(CustomOrderStatus.CREATED);

        order.cancelAfterWarehouseRejection("missing components");
        order.cancelAfterWarehouseRejection("missing components");

        assertThat(order.getStatus()).isEqualTo(CustomOrderStatus.CANCELED);
    }

    private InStockCarOrder inStockOrder(InStockOrderStatus status) {
        return new InStockCarOrder(
                UUID.randomUUID(),
                mock(Client.class),
                mock(SalesManager.class),
                UUID.randomUUID(),
                "Car",
                status
        );
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
