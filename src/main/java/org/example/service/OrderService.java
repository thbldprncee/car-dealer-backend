package org.example.service;

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
import org.example.repository.CarRepository;
import org.example.repository.CustomCarOrderRepository;
import org.example.repository.InStockCarOrderRepository;
import org.example.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderService {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final CarConfigurationRepository carConfigurationRepository;
    private final InStockCarOrderRepository inStockCarOrderRepository;
    private final CustomCarOrderRepository customCarOrderRepository;
    private final InStockCarOrderMapper inStockCarOrderMapper;
    private final CustomCarOrderMapper customCarOrderMapper;

    public OrderService(
            UserRepository userRepository,
            CarRepository carRepository,
            CarConfigurationRepository carConfigurationRepository,
            InStockCarOrderRepository inStockCarOrderRepository,
            CustomCarOrderRepository customCarOrderRepository
    ) {
        this.userRepository = userRepository;
        this.carRepository = carRepository;
        this.carConfigurationRepository = carConfigurationRepository;
        this.inStockCarOrderRepository = inStockCarOrderRepository;
        this.customCarOrderRepository = customCarOrderRepository;
        this.inStockCarOrderMapper = InStockCarOrderMapper.INSTANCE;
        this.customCarOrderMapper = CustomCarOrderMapper.INSTANCE;
    }

    public InStockCarOrderResponse createInStockOrder(CreateInStockCarOrderRequest request) {
        validateCreateInStockOrderRequest(request);

        Client client = userRepository.findById(request.clientId())
                .filter(Client.class::isInstance)
                .map(Client.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Client", request.clientId()));

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new EntityNotFoundException("Car", request.carId()));

        SalesManager salesManager = assignSalesManager();

        InStockCarOrder order = new InStockCarOrder(
                UUID.randomUUID(),
                client,
                salesManager,
                car,
                LocalDateTime.now(),
                InStockOrderStatus.CREATED
        );

        car.markAsUnavailable();
        car.disableTestDrive();

        carRepository.save(car);
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    public CustomCarOrderResponse createCustomOrder(CreateCustomCarOrderRequest request) {
        validateCreateCustomOrderRequest(request);

        Client client = userRepository.findById(request.clientId())
                .filter(Client.class::isInstance)
                .map(Client.class::cast)
                .orElseThrow(() -> new EntityNotFoundException("Client", request.clientId()));

        CarConfiguration configuration = carConfigurationRepository.findById(request.carConfigurationId())
                .orElseThrow(() -> new EntityNotFoundException("CarConfiguration", request.carConfigurationId()));

        CarModel carModel = configuration.getCarModel();

        if (!carModel.getId().equals(request.carModelId())) {
            throw new DomainValidationException("Configuration car model does not match requested car model");
        }

        SalesManager salesManager = assignSalesManager();

        CustomCarOrder order = new CustomCarOrder(
                UUID.randomUUID(),
                client,
                salesManager,
                carModel,
                configuration,
                LocalDateTime.now(),
                CustomOrderStatus.CREATED
        );

        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    public InStockCarOrderResponse getInStockOrderById(UUID id) {
        return inStockCarOrderRepository.findById(id)
                .map(inStockCarOrderMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", id));
    }

    public CustomCarOrderResponse getCustomOrderById(UUID id) {
        return customCarOrderRepository.findById(id)
                .map(customCarOrderMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", id));
    }

    public List<InStockCarOrderResponse> getAllInStockOrders() {
        return inStockCarOrderRepository.findAll().stream()
                .map(inStockCarOrderMapper::toResponse)
                .toList();
    }

    public List<CustomCarOrderResponse> getAllCustomOrders() {
        return customCarOrderRepository.findAll().stream()
                .map(customCarOrderMapper::toResponse)
                .toList();
    }

    public InStockCarOrderResponse approveInStockOrderByManager(UUID orderId) {
        InStockCarOrder order = inStockCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", orderId));

        order.approveByManager();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    public InStockCarOrderResponse moveInStockOrderToWaitingForPayment(UUID orderId) {
        InStockCarOrder order = inStockCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", orderId));

        order.markWaitingForPayment();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    public InStockCarOrderResponse payInStockOrder(UUID orderId) {
        InStockCarOrder order = inStockCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", orderId));

        order.markAsPaid();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    public InStockCarOrderResponse markInStockOrderReadyForDelivery(UUID orderId) {
        InStockCarOrder order = inStockCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", orderId));

        order.markReadyForDelivery();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    public InStockCarOrderResponse completeInStockOrder(UUID orderId) {
        InStockCarOrder order = inStockCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", orderId));

        order.complete();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    public InStockCarOrderResponse cancelInStockOrder(UUID orderId) {
        InStockCarOrder order = inStockCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", orderId));

        order.cancel();
        return inStockCarOrderMapper.toResponse(inStockCarOrderRepository.save(order));
    }

    public CustomCarOrderResponse approveCustomOrderByWarehouse(UUID orderId) {
        CustomCarOrder order = customCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));

        order.approveByWarehouse();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    public CustomCarOrderResponse moveCustomOrderToWaitingForPayment(UUID orderId) {
        CustomCarOrder order = customCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));

        order.markWaitingForPayment();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    public CustomCarOrderResponse payCustomOrder(UUID orderId) {
        CustomCarOrder order = customCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));

        order.markAsPaid();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    public CustomCarOrderResponse moveCustomOrderToWaitingForDelivery(UUID orderId) {
        CustomCarOrder order = customCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));

        order.markWaitingForDelivery();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    public CustomCarOrderResponse markCustomOrderReadyForDelivery(UUID orderId) {
        CustomCarOrder order = customCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));

        order.markReadyForDelivery();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    public CustomCarOrderResponse completeCustomOrder(UUID orderId) {
        CustomCarOrder order = customCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));

        order.complete();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    public CustomCarOrderResponse cancelCustomOrder(UUID orderId) {
        CustomCarOrder order = customCarOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", orderId));

        order.cancel();
        return customCarOrderMapper.toResponse(customCarOrderRepository.save(order));
    }

    private SalesManager assignSalesManager() {
        return userRepository.findAllSalesManagers().stream()
                .findFirst()
                .orElseThrow(() -> new DomainValidationException("No sales manager is available for assignment"));
    }

    private void validateCreateInStockOrderRequest(CreateInStockCarOrderRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create in-stock order request must not be null");
        }
    }

    private void validateCreateCustomOrderRequest(CreateCustomCarOrderRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create custom order request must not be null");
        }
    }
}