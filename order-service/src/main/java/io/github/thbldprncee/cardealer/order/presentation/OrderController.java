package io.github.thbldprncee.cardealer.order.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.github.thbldprncee.cardealer.order.domain.enums.CustomOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.enums.InStockOrderStatus;
import io.github.thbldprncee.cardealer.order.dto.request.CreateCustomCarOrderRequest;
import io.github.thbldprncee.cardealer.order.dto.request.CreateInStockCarOrderRequest;
import io.github.thbldprncee.cardealer.order.dto.response.CustomCarOrderResponse;
import io.github.thbldprncee.cardealer.order.dto.response.InStockCarOrderResponse;
import io.github.thbldprncee.cardealer.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/in-stock")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public InStockCarOrderResponse createInStockOrder(
            @Valid @RequestBody CreateInStockCarOrderRequest request
    ) {
        return orderService.createInStockOrder(request);
    }

    @GetMapping("/in-stock/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public InStockCarOrderResponse getInStockOrderById(@PathVariable UUID id) {
        return orderService.getInStockOrderById(id);
    }

    @GetMapping("/in-stock")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<InStockCarOrderResponse> getAllInStockOrders(
            @RequestParam(required = false) InStockOrderStatus status
    ) {
        if (status == null) {
            return orderService.getAllInStockOrders();
        }

        return orderService.getInStockOrdersByStatus(status);
    }

    @PatchMapping("/in-stock/{id}/approve-by-manager")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InStockCarOrderResponse approveInStockOrderByManager(@PathVariable UUID id) {
        return orderService.approveInStockOrderByManager(id);
    }

    @PatchMapping("/in-stock/{id}/waiting-for-payment")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InStockCarOrderResponse moveInStockOrderToWaitingForPayment(@PathVariable UUID id) {
        return orderService.moveInStockOrderToWaitingForPayment(id);
    }

    @PatchMapping("/in-stock/{id}/pay")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public InStockCarOrderResponse payInStockOrder(@PathVariable UUID id) {
        return orderService.payInStockOrder(id);
    }

    @PatchMapping("/in-stock/{id}/complete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InStockCarOrderResponse completeInStockOrder(@PathVariable UUID id) {
        return orderService.completeInStockOrder(id);
    }

    @PatchMapping("/in-stock/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public InStockCarOrderResponse cancelInStockOrder(@PathVariable UUID id) {
        return orderService.cancelInStockOrder(id);
    }

    @PostMapping("/custom")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CustomCarOrderResponse createCustomOrder(
            @Valid @RequestBody CreateCustomCarOrderRequest request
    ) {
        return orderService.createCustomOrder(request);
    }

    @GetMapping("/custom/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public CustomCarOrderResponse getCustomOrderById(@PathVariable UUID id) {
        return orderService.getCustomOrderById(id);
    }

    @GetMapping("/custom")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<CustomCarOrderResponse> getAllCustomOrders(
            @RequestParam(required = false) CustomOrderStatus status
    ) {
        if (status == null) {
            return orderService.getAllCustomOrders();
        }

        return orderService.getCustomOrdersByStatus(status);
    }

    @PatchMapping("/custom/{id}/waiting-for-payment")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomCarOrderResponse moveCustomOrderToWaitingForPayment(@PathVariable UUID id) {
        return orderService.moveCustomOrderToWaitingForPayment(id);
    }

    @PatchMapping("/custom/{id}/pay")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CustomCarOrderResponse payCustomOrder(@PathVariable UUID id) {
        return orderService.payCustomOrder(id);
    }

    @PatchMapping("/custom/{id}/waiting-for-delivery")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomCarOrderResponse moveCustomOrderToWaitingForDelivery(@PathVariable UUID id) {
        return orderService.moveCustomOrderToWaitingForDelivery(id);
    }

    @PatchMapping("/custom/{id}/ready-for-delivery")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomCarOrderResponse markCustomOrderReadyForDelivery(@PathVariable UUID id) {
        return orderService.markCustomOrderReadyForDelivery(id);
    }

    @PatchMapping("/custom/{id}/complete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomCarOrderResponse completeCustomOrder(@PathVariable UUID id) {
        return orderService.completeCustomOrder(id);
    }

    @PatchMapping("/custom/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CustomCarOrderResponse cancelCustomOrder(@PathVariable UUID id) {
        return orderService.cancelCustomOrder(id);
    }
}
