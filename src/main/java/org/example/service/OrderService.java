package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.config.security.CurrentUserService;
import org.example.domain.enums.CustomOrderStatus;
import org.example.domain.enums.InStockOrderStatus;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.model.Car;
import org.example.domain.model.CarConfiguration;
import org.example.domain.model.CarModel;
import org.example.domain.model.Client;
import org.example.domain.model.CustomCarOrder;
import org.example.domain.model.InStockCarOrder;
import org.example.domain.model.SalesManager;
import org.example.dto.request.CreateCustomCarOrderRequest;
import org.example.dto.request.CreateInStockCarOrderRequest;
import org.example.dto.response.CustomCarOrderResponse;
import org.example.dto.response.InStockCarOrderResponse;
import org.example.mappers.CustomCarOrderMapper;
import org.example.mappers.InStockCarOrderMapper;
import org.example.repository.CarConfigurationRepository;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.ClientRepository;
import org.example.repository.CustomCarOrderRepository;
import org.example.repository.InStockCarOrderRepository;
import org.example.repository.SalesManagerRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final CurrentUserService currentUserService;

    private final ClientRepository clientRepository;
    private final SalesManagerRepository salesManagerRepository;
    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final CarConfigurationRepository carConfigurationRepository;
    private final InStockCarOrderRepository inStockCarOrderRepository;
    private final CustomCarOrderRepository customCarOrderRepository;

    private final InStockCarOrderMapper inStockCarOrderMapper = InStockCarOrderMapper.INSTANCE;
    private final CustomCarOrderMapper customCarOrderMapper = CustomCarOrderMapper.INSTANCE;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public InStockCarOrderResponse createInStockOrder(CreateInStockCarOrderRequest request) {
        validateCreateInStockOrderRequest(request);

        UUID currentUserId = currentUserService.getCurrentUserId();

        Client client = clientRepository.findById(currentUserId)
                .filter(existingClient -> !existingClient.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Client", currentUserId));

        Car car = carRepository.findById(request.carId())
                .filter(existingCar -> !existingCar.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Car", request.carId()));

        SalesManager salesManager = assignSalesManager();

        InStockCarOrder order = new InStockCarOrder(
                UUID.randomUUID(),
                client,
                salesManager,
                car,
                InStockOrderStatus.CREATED
        );

        car.markAsUnavailable();
        car.disableTestDrive();

        carRepository.save(car);
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CustomCarOrderResponse createCustomOrder(CreateCustomCarOrderRequest request) {
        validateCreateCustomOrderRequest(request);

        UUID currentUserId = currentUserService.getCurrentUserId();

        Client client = clientRepository.findById(currentUserId)
                .filter(existingClient -> !existingClient.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("Client", currentUserId));

        CarModel carModel = carModelRepository.findById(request.carModelId())
                .filter(existingCarModel -> !existingCarModel.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("CarModel", request.carModelId()));

        CarConfiguration configuration = carConfigurationRepository.findById(request.carConfigurationId())
                .filter(existingConfiguration -> !existingConfiguration.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("CarConfiguration", request.carConfigurationId()));

        if (!configuration.getCarModel().getId().equals(carModel.getId())) {
            throw new DomainValidationException("Configuration car model does not match requested car model");
        }

        SalesManager salesManager = assignSalesManager();

        CustomCarOrder order = new CustomCarOrder(
                UUID.randomUUID(),
                client,
                salesManager,
                carModel,
                configuration,
                CustomOrderStatus.CREATED
        );

        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityService.isInStockOrderOwner(#id)")
    public InStockCarOrderResponse getInStockOrderById(UUID id) {
        return inStockCarOrderRepository.findById(id)
                .filter(order -> !order.isRemoved())
                .map(inStockCarOrderMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", id));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityService.isCustomOrderOwner(#id)")
    public CustomCarOrderResponse getCustomOrderById(UUID id) {
        return customCarOrderRepository.findById(id)
                .filter(order -> !order.isRemoved())
                .map(customCarOrderMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", id));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<InStockCarOrderResponse> getAllInStockOrders() {
        return inStockCarOrderRepository.findAllByRemovedFalse().stream()
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

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public List<InStockCarOrderResponse> getInStockOrdersByStatus(InStockOrderStatus status) {
        if (status == null) {
            throw new DomainValidationException("In-stock order status must not be null");
        }

        return inStockCarOrderRepository.findAllByStatusAndRemovedFalse(status).stream()
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
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public InStockCarOrderResponse markInStockOrderReadyForDelivery(UUID orderId) {
        InStockCarOrder order = findActiveInStockOrder(orderId);
        order.markReadyForDelivery();
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
        order.cancel();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CustomCarOrderResponse approveCustomOrderByWarehouse(UUID orderId) {
        CustomCarOrder order = findActiveCustomOrder(orderId);
        order.approveByWarehouse();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
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
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CustomCarOrderResponse moveCustomOrderToWaitingForDelivery(UUID orderId) {
        CustomCarOrder order = findActiveCustomOrder(orderId);
        order.markWaitingForDelivery();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
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
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    private InStockCarOrder findActiveInStockOrder(UUID orderId) {
        return inStockCarOrderRepository.findById(orderId)
                .filter(order -> !order.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", orderId));
    }

    private CustomCarOrder findActiveCustomOrder(UUID orderId) {
        return customCarOrderRepository.findById(orderId)
                .filter(order -> !order.isRemoved())
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));
    }

    private SalesManager assignSalesManager() {
        return salesManagerRepository.findAllByRemovedFalse().stream()
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("No sales manager is available for assignment"));
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