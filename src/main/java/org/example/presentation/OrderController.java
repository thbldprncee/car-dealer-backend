package org.example.presentation;

import lombok.RequiredArgsConstructor;
import org.example.domain.enums.CustomOrderStatus;
import org.example.domain.enums.InStockOrderStatus;
import org.example.dto.request.CreateCustomCarOrderRequest;
import org.example.dto.request.CreateInStockCarOrderRequest;
import org.example.dto.response.CustomCarOrderResponse;
import org.example.dto.response.InStockCarOrderResponse;
import org.example.service.OrderService;
import org.springframework.http.HttpStatus;
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
    public InStockCarOrderResponse createInStockOrder(
            @RequestBody CreateInStockCarOrderRequest request
    ) {
        return orderService.createInStockOrder(request);
    }

    @GetMapping("/in-stock/{id}")
    public InStockCarOrderResponse getInStockOrderById(@PathVariable UUID id) {
        return orderService.getInStockOrderById(id);
    }

    @GetMapping("/in-stock")
    public List<InStockCarOrderResponse> getAllInStockOrders(
            @RequestParam(required = false) InStockOrderStatus status
    ) {
        if (status == null) {
            return orderService.getAllInStockOrders();
        }

        return orderService.getInStockOrdersByStatus(status);
    }

    @PatchMapping("/in-stock/{id}/approve-by-manager")
    public InStockCarOrderResponse approveInStockOrderByManager(@PathVariable UUID id) {
        return orderService.approveInStockOrderByManager(id);
    }

    @PatchMapping("/in-stock/{id}/waiting-for-payment")
    public InStockCarOrderResponse moveInStockOrderToWaitingForPayment(@PathVariable UUID id) {
        return orderService.moveInStockOrderToWaitingForPayment(id);
    }

    @PatchMapping("/in-stock/{id}/pay")
    public InStockCarOrderResponse payInStockOrder(@PathVariable UUID id) {
        return orderService.payInStockOrder(id);
    }

    @PatchMapping("/in-stock/{id}/ready-for-delivery")
    public InStockCarOrderResponse markInStockOrderReadyForDelivery(@PathVariable UUID id) {
        return orderService.markInStockOrderReadyForDelivery(id);
    }

    @PatchMapping("/in-stock/{id}/complete")
    public InStockCarOrderResponse completeInStockOrder(@PathVariable UUID id) {
        return orderService.completeInStockOrder(id);
    }

    @PatchMapping("/in-stock/{id}/cancel")
    public InStockCarOrderResponse cancelInStockOrder(@PathVariable UUID id) {
        return orderService.cancelInStockOrder(id);
    }

    @PostMapping("/custom")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomCarOrderResponse createCustomOrder(
            @RequestBody CreateCustomCarOrderRequest request
    ) {
        return orderService.createCustomOrder(request);
    }

    @GetMapping("/custom/{id}")
    public CustomCarOrderResponse getCustomOrderById(@PathVariable UUID id) {
        return orderService.getCustomOrderById(id);
    }

    @GetMapping("/custom")
    public List<CustomCarOrderResponse> getAllCustomOrders(
            @RequestParam(required = false) CustomOrderStatus status
    ) {
        if (status == null) {
            return orderService.getAllCustomOrders();
        }

        return orderService.getCustomOrdersByStatus(status);
    }

    @PatchMapping("/custom/{id}/approve-by-warehouse")
    public CustomCarOrderResponse approveCustomOrderByWarehouse(@PathVariable UUID id) {
        return orderService.approveCustomOrderByWarehouse(id);
    }

    @PatchMapping("/custom/{id}/waiting-for-payment")
    public CustomCarOrderResponse moveCustomOrderToWaitingForPayment(@PathVariable UUID id) {
        return orderService.moveCustomOrderToWaitingForPayment(id);
    }

    @PatchMapping("/custom/{id}/pay")
    public CustomCarOrderResponse payCustomOrder(@PathVariable UUID id) {
        return orderService.payCustomOrder(id);
    }

    @PatchMapping("/custom/{id}/waiting-for-delivery")
    public CustomCarOrderResponse moveCustomOrderToWaitingForDelivery(@PathVariable UUID id) {
        return orderService.moveCustomOrderToWaitingForDelivery(id);
    }

    @PatchMapping("/custom/{id}/ready-for-delivery")
    public CustomCarOrderResponse markCustomOrderReadyForDelivery(@PathVariable UUID id) {
        return orderService.markCustomOrderReadyForDelivery(id);
    }

    @PatchMapping("/custom/{id}/complete")
    public CustomCarOrderResponse completeCustomOrder(@PathVariable UUID id) {
        return orderService.completeCustomOrder(id);
    }

    @PatchMapping("/custom/{id}/cancel")
    public CustomCarOrderResponse cancelCustomOrder(@PathVariable UUID id) {
        return orderService.cancelCustomOrder(id);
    }
}