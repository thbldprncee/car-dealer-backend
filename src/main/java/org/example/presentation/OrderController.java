package org.example.presentation;

import org.example.dto.request.CreateCustomCarOrderRequest;
import org.example.dto.request.CreateInStockCarOrderRequest;
import org.example.dto.response.CustomCarOrderResponse;
import org.example.dto.response.InStockCarOrderResponse;
import org.example.service.OrderService;

import java.util.List;
import java.util.UUID;

public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public InStockCarOrderResponse createInStockOrder(CreateInStockCarOrderRequest request) {
        return orderService.createInStockOrder(request);
    }

    public CustomCarOrderResponse createCustomOrder(CreateCustomCarOrderRequest request) {
        return orderService.createCustomOrder(request);
    }

    public InStockCarOrderResponse getInStockOrderById(UUID id) {
        return orderService.getInStockOrderById(id);
    }

    public CustomCarOrderResponse getCustomOrderById(UUID id) {
        return orderService.getCustomOrderById(id);
    }

    public List<InStockCarOrderResponse> getAllInStockOrders() {
        return orderService.getAllInStockOrders();
    }

    public List<CustomCarOrderResponse> getAllCustomOrders() {
        return orderService.getAllCustomOrders();
    }

    public InStockCarOrderResponse approveInStockOrderByManager(UUID orderId) {
        return orderService.approveInStockOrderByManager(orderId);
    }

    public InStockCarOrderResponse moveInStockOrderToWaitingForPayment(UUID orderId) {
        return orderService.moveInStockOrderToWaitingForPayment(orderId);
    }

    public InStockCarOrderResponse payInStockOrder(UUID orderId) {
        return orderService.payInStockOrder(orderId);
    }

    public InStockCarOrderResponse markInStockOrderReadyForDelivery(UUID orderId) {
        return orderService.markInStockOrderReadyForDelivery(orderId);
    }

    public InStockCarOrderResponse completeInStockOrder(UUID orderId) {
        return orderService.completeInStockOrder(orderId);
    }

    public InStockCarOrderResponse cancelInStockOrder(UUID orderId) {
        return orderService.cancelInStockOrder(orderId);
    }

    public CustomCarOrderResponse approveCustomOrderByWarehouse(UUID orderId) {
        return orderService.approveCustomOrderByWarehouse(orderId);
    }

    public CustomCarOrderResponse moveCustomOrderToWaitingForPayment(UUID orderId) {
        return orderService.moveCustomOrderToWaitingForPayment(orderId);
    }

    public CustomCarOrderResponse payCustomOrder(UUID orderId) {
        return orderService.payCustomOrder(orderId);
    }

    public CustomCarOrderResponse moveCustomOrderToWaitingForDelivery(UUID orderId) {
        return orderService.moveCustomOrderToWaitingForDelivery(orderId);
    }

    public CustomCarOrderResponse markCustomOrderReadyForDelivery(UUID orderId) {
        return orderService.markCustomOrderReadyForDelivery(orderId);
    }

    public CustomCarOrderResponse completeCustomOrder(UUID orderId) {
        return orderService.completeCustomOrder(orderId);
    }

    public CustomCarOrderResponse cancelCustomOrder(UUID orderId) {
        return orderService.cancelCustomOrder(orderId);
    }
}