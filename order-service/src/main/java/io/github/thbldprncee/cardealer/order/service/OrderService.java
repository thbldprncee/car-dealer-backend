package io.github.thbldprncee.cardealer.order.service;

import lombok.RequiredArgsConstructor;
import io.github.thbldprncee.cardealer.order.client.StorageServiceClient;
import io.github.thbldprncee.cardealer.order.config.TraceIdProvider;
import io.github.thbldprncee.cardealer.order.config.security.CurrentUserService;
import io.github.thbldprncee.cardealer.order.domain.enums.CustomOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.enums.InStockOrderStatus;
import io.github.thbldprncee.cardealer.order.domain.exceptions.DomainValidationException;
import io.github.thbldprncee.cardealer.order.domain.exceptions.EntityNotFoundException;
import io.github.thbldprncee.cardealer.order.domain.model.Client;
import io.github.thbldprncee.cardealer.order.domain.model.CustomCarOrder;
import io.github.thbldprncee.cardealer.order.domain.model.InStockCarOrder;
import io.github.thbldprncee.cardealer.order.domain.model.SalesManager;
import io.github.thbldprncee.cardealer.order.dto.request.CreateCustomCarOrderRequest;
import io.github.thbldprncee.cardealer.order.dto.request.CreateInStockCarOrderRequest;
import io.github.thbldprncee.cardealer.order.dto.response.CustomCarOrderResponse;
import io.github.thbldprncee.cardealer.order.dto.response.InStockCarOrderResponse;
import io.github.thbldprncee.cardealer.events.OrderCanceledEvent;
import io.github.thbldprncee.cardealer.events.OrderSentForApprovalEvent;
import io.github.thbldprncee.cardealer.events.OrderType;
import io.github.thbldprncee.cardealer.order.mappers.CustomCarOrderMapper;
import io.github.thbldprncee.cardealer.order.mappers.InStockCarOrderMapper;
import io.github.thbldprncee.cardealer.order.repository.ClientRepository;
import io.github.thbldprncee.cardealer.order.repository.CustomCarOrderRepository;
import io.github.thbldprncee.cardealer.order.repository.InStockCarOrderRepository;
import io.github.thbldprncee.cardealer.order.repository.SalesManagerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    @Value("${app.kafka.topics.order-sent-for-approval:order.sent-for-approval}")
    private String orderSentForApprovalTopic;

    @Value("${app.kafka.topics.order-canceled:order.canceled}")
    private String orderCanceledTopic;

    private final CurrentUserService currentUserService;
    private final TraceIdProvider traceIdProvider;

    private final ClientRepository clientRepository;
    private final SalesManagerRepository salesManagerRepository;
    private final InStockCarOrderRepository inStockCarOrderRepository;
    private final CustomCarOrderRepository customCarOrderRepository;

    private final InStockCarOrderMapper inStockCarOrderMapper;
    private final CustomCarOrderMapper customCarOrderMapper;
    private final OutboxEventService outboxEventService;
    private final StorageServiceClient storageServiceClient;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public InStockCarOrderResponse createInStockOrder(CreateInStockCarOrderRequest request) {
        validateCreateInStockOrderRequest(request);

        UUID currentUserId = currentUserService.getCurrentUserId();

        Client client = clientRepository.findByIdAndRemovedFalse(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Client", currentUserId));

        SalesManager salesManager = assignSalesManager();
        StorageServiceClient.CarSnapshot car = storageServiceClient.getCar(request.carId());

        InStockCarOrder order = new InStockCarOrder(
                UUID.randomUUID(),
                client,
                salesManager,
                request.carId(),
                car.displayName(),
                InStockOrderStatus.CREATED
        );

        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CustomCarOrderResponse createCustomOrder(CreateCustomCarOrderRequest request) {
        validateCreateCustomOrderRequest(request);

        UUID currentUserId = currentUserService.getCurrentUserId();

        Client client = clientRepository.findByIdAndRemovedFalse(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Client", currentUserId));

        SalesManager salesManager = assignSalesManager();
        StorageServiceClient.CarConfigurationSnapshot configuration =
                storageServiceClient.getConfiguration(request.carConfigurationId());

        if (!request.carModelId().equals(configuration.carModelId())) {
            throw new DomainValidationException("Configuration belongs to another car model");
        }

        CustomCarOrder order = new CustomCarOrder(
                UUID.randomUUID(),
                client,
                salesManager,
                request.carModelId(),
                configuration.carModelDisplayName(),
                request.carConfigurationId(),
                configuration.totalPrice(),
                CustomOrderStatus.CREATED
        );

        // The warehouse checks that every component of the configuration can be supplied;
        // its answer moves the order to APPROVED_BY_WAREHOUSE or cancels it.
        saveOrderSentForApprovalEvent(order);

        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityService.isInStockOrderOwner(#id)")
    public InStockCarOrderResponse getInStockOrderById(UUID id) {
        return inStockCarOrderRepository.findByIdAndRemovedFalse(id)
                .map(inStockCarOrderMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", id));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityService.isCustomOrderOwner(#id)")
    public CustomCarOrderResponse getCustomOrderById(UUID id) {
        return customCarOrderRepository.findByIdAndRemovedFalse(id)
                .map(customCarOrderMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", id));
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<InStockCarOrderResponse> getAllInStockOrders() {
        if (currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            return inStockCarOrderRepository.findAllByRemovedFalse().stream()
                    .map(inStockCarOrderMapper::toResponse)
                    .toList();
        }

        UUID currentUserId = currentUserService.getCurrentUserId();

        return inStockCarOrderRepository.findAllByClientIdAndRemovedFalse(currentUserId).stream()
                .map(inStockCarOrderMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<CustomCarOrderResponse> getAllCustomOrders() {
        if (currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            return customCarOrderRepository.findAllByRemovedFalse().stream()
                    .map(customCarOrderMapper::toResponse)
                    .toList();
        }

        UUID currentUserId = currentUserService.getCurrentUserId();

        return customCarOrderRepository.findAllByClientIdAndRemovedFalse(currentUserId).stream()
                .map(customCarOrderMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<InStockCarOrderResponse> getInStockOrdersByStatus(InStockOrderStatus status) {
        if (status == null) {
            throw new DomainValidationException("In-stock order status must not be null");
        }

        if (currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            return inStockCarOrderRepository.findAllByStatusAndRemovedFalse(status).stream()
                    .map(inStockCarOrderMapper::toResponse)
                    .toList();
        }

        UUID currentUserId = currentUserService.getCurrentUserId();

        return inStockCarOrderRepository.findAllByClientIdAndStatusAndRemovedFalse(currentUserId, status).stream()
                .map(inStockCarOrderMapper::toResponse)
                .toList();
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<CustomCarOrderResponse> getCustomOrdersByStatus(CustomOrderStatus status) {
        if (status == null) {
            throw new DomainValidationException("Custom order status must not be null");
        }

        if (currentUserService.hasAnyRole("MANAGER", "ADMIN")) {
            return customCarOrderRepository.findAllByStatusAndRemovedFalse(status).stream()
                    .map(customCarOrderMapper::toResponse)
                    .toList();
        }

        UUID currentUserId = currentUserService.getCurrentUserId();

        return customCarOrderRepository.findAllByClientIdAndStatusAndRemovedFalse(currentUserId, status).stream()
                .map(customCarOrderMapper::toResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InStockCarOrderResponse approveInStockOrderByManager(UUID orderId) {
        InStockCarOrder order = findActiveInStockOrder(orderId);
        order.approveByManager();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InStockCarOrderResponse moveInStockOrderToWaitingForPayment(UUID orderId) {
        InStockCarOrder order = findActiveInStockOrder(orderId);
        order.markWaitingForPayment();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isInStockOrderOwner(#orderId)")
    public InStockCarOrderResponse payInStockOrder(UUID orderId) {
        InStockCarOrder order = findActiveInStockOrder(orderId);
        order.markAsPaid();
        saveOrderSentForApprovalEvent(order);
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public InStockCarOrderResponse completeInStockOrder(UUID orderId) {
        InStockCarOrder order = findActiveInStockOrder(orderId);
        order.complete();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isInStockOrderOwner(#orderId)")
    public InStockCarOrderResponse cancelInStockOrder(UUID orderId) {
        InStockCarOrder order = findActiveInStockOrder(orderId);
        boolean sentToWarehouse = order.isSentToWarehouse();
        order.cancel();
        if (sentToWarehouse) {
            saveOrderCanceledEvent(order.getId(), OrderType.IN_STOCK, order.getCarId(), "InStockCarOrder");
        }
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomCarOrderResponse moveCustomOrderToWaitingForPayment(UUID orderId) {
        CustomCarOrder order = findActiveCustomOrder(orderId);
        order.markWaitingForPayment();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isCustomOrderOwner(#orderId)")
    public CustomCarOrderResponse payCustomOrder(UUID orderId) {
        CustomCarOrder order = findActiveCustomOrder(orderId);
        order.markAsPaid();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomCarOrderResponse moveCustomOrderToWaitingForDelivery(UUID orderId) {
        CustomCarOrder order = findActiveCustomOrder(orderId);
        order.markWaitingForDelivery();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomCarOrderResponse markCustomOrderReadyForDelivery(UUID orderId) {
        CustomCarOrder order = findActiveCustomOrder(orderId);
        order.markReadyForDelivery();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomCarOrderResponse completeCustomOrder(UUID orderId) {
        CustomCarOrder order = findActiveCustomOrder(orderId);
        order.complete();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @orderSecurityService.isCustomOrderOwner(#orderId)")
    public CustomCarOrderResponse cancelCustomOrder(UUID orderId) {
        CustomCarOrder order = findActiveCustomOrder(orderId);
        order.cancel();
        // Custom orders are always sent to the warehouse when created.
        saveOrderCanceledEvent(order.getId(), OrderType.CUSTOM, null, "CustomCarOrder");
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    private InStockCarOrder findActiveInStockOrder(UUID orderId) {
        return inStockCarOrderRepository.findByIdAndRemovedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", orderId));
    }

    private CustomCarOrder findActiveCustomOrder(UUID orderId) {
        return customCarOrderRepository.findByIdAndRemovedFalse(orderId)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));
    }

    private SalesManager assignSalesManager() {
        return salesManagerRepository.findAllByRemovedFalse().stream()
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("No sales manager is available for assignment"));
    }

    private void saveOrderSentForApprovalEvent(InStockCarOrder order) {
        String traceId = traceIdProvider.getTraceId();
        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                UUID.randomUUID(),
                order.getId(),
                OrderType.IN_STOCK,
                order.getCarId(),
                null,
                null,
                List.of(),
                traceId,
                Instant.now()
        );

        outboxEventService.saveEvent(
                order.getId(),
                "InStockCarOrder",
                OrderSentForApprovalEvent.class.getSimpleName(),
                orderSentForApprovalTopic,
                event,
                traceId
        );
    }

    private void saveOrderSentForApprovalEvent(CustomCarOrder order) {
        String traceId = traceIdProvider.getTraceId();
        OrderSentForApprovalEvent event = new OrderSentForApprovalEvent(
                UUID.randomUUID(),
                order.getId(),
                OrderType.CUSTOM,
                null,
                order.getCarModelId(),
                order.getCarConfigurationId(),
                List.of(),
                traceId,
                Instant.now()
        );

        outboxEventService.saveEvent(
                order.getId(),
                "CustomCarOrder",
                OrderSentForApprovalEvent.class.getSimpleName(),
                orderSentForApprovalTopic,
                event,
                traceId
        );
    }

    private void saveOrderCanceledEvent(UUID orderId, OrderType orderType, UUID carId, String aggregateType) {
        String traceId = traceIdProvider.getTraceId();
        OrderCanceledEvent event = new OrderCanceledEvent(
                UUID.randomUUID(),
                orderId,
                orderType,
                carId,
                traceId,
                Instant.now()
        );

        outboxEventService.saveEvent(
                orderId,
                aggregateType,
                OrderCanceledEvent.class.getSimpleName(),
                orderCanceledTopic,
                event,
                traceId
        );
    }

    private void validateCreateInStockOrderRequest(CreateInStockCarOrderRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create in-stock order request must not be null");
        }

        if (request.carId() == null) {
            throw new DomainValidationException("Car id must not be null");
        }
    }

    private void validateCreateCustomOrderRequest(CreateCustomCarOrderRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create custom order request must not be null");
        }

        if (request.carModelId() == null) {
            throw new DomainValidationException("Car model id must not be null");
        }

        if (request.carConfigurationId() == null) {
            throw new DomainValidationException("Car configuration id must not be null");
        }
    }
}
