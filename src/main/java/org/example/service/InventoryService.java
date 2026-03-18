package org.example.service;

import org.example.domain.enums.ComponentType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.model.Car;
import org.example.domain.model.CarModel;
import org.example.domain.model.ComponentOption;
import org.example.domain.model.SparePart;
import org.example.dto.request.CreateCarModelRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.CreateComponentOptionRequest;
import org.example.dto.request.CreateSparePartRequest;
import org.example.dto.request.UpdateCarRequest;
import org.example.dto.request.UpdateSparePartRequest;
import org.example.dto.response.CarModelResponse;
import org.example.dto.response.CarResponse;
import org.example.dto.response.ComponentOptionResponse;
import org.example.dto.response.SparePartResponse;
import org.example.mappers.CarMapper;
import org.example.mappers.CarModelMapper;
import org.example.mappers.ComponentOptionMapper;
import org.example.mappers.SparePartMapper;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.ComponentOptionRepository;
import org.example.repository.SparePartRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InventoryService {

    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final SparePartRepository sparePartRepository;
    private final ComponentOptionRepository componentOptionRepository;

    private final CarMapper carMapper;
    private final CarModelMapper carModelMapper;
    private final SparePartMapper sparePartMapper;
    private final ComponentOptionMapper componentOptionMapper;

    public InventoryService(
            CarRepository carRepository,
            CarModelRepository carModelRepository,
            SparePartRepository sparePartRepository,
            ComponentOptionRepository componentOptionRepository
    ) {
        this.carRepository = carRepository;
        this.carModelRepository = carModelRepository;
        this.sparePartRepository = sparePartRepository;
        this.componentOptionRepository = componentOptionRepository;
        this.carMapper = CarMapper.INSTANCE;
        this.carModelMapper = CarModelMapper.INSTANCE;
        this.sparePartMapper = SparePartMapper.INSTANCE;
        this.componentOptionMapper = ComponentOptionMapper.INSTANCE;
    }

    public CarModelResponse createCarModel(CreateCarModelRequest request) {
        validateCreateCarModelRequest(request);

        Map<ComponentType, ComponentOption> baseComponents = new EnumMap<>(ComponentType.class);

        request.baseComponentIds().forEach((componentType, componentId) -> {
            ComponentOption componentOption = componentOptionRepository.findById(componentId)
                    .orElseThrow(() -> new EntityNotFoundException("ComponentOption", componentId));
            baseComponents.put(componentType, componentOption);
        });

        CarModel carModel = new CarModel(
                request.id(),
                request.brand(),
                request.modelName(),
                request.bodyType(),
                request.fuelType(),
                request.enginePower(),
                request.engineVolume(),
                request.transmissionType(),
                request.driveType(),
                request.basePrice(),
                request.requiredComponentTypes(),
                baseComponents
        );

        return carModelMapper.toResponse(carModelRepository.save(carModel));
    }

    public ComponentOptionResponse createComponentOption(CreateComponentOptionRequest request) {
        validateCreateComponentOptionRequest(request);

        ComponentOption componentOption = new ComponentOption(
                UUID.randomUUID(),
                request.componentType(),
                request.name(),
                request.priceDelta(),
                request.compatibleCarModelIds()
        );

        return componentOptionMapper.toResponse(componentOptionRepository.save(componentOption));
    }

    public CarResponse createCar(CreateCarRequest request) {
        validateCreateCarRequest(request);

        carRepository.findByVin(request.vin())
                .ifPresent(existingCar -> {
                    throw new DomainValidationException(
                            "Car with vin=%s already exists".formatted(request.vin())
                    );
                });

        CarModel carModel = carModelRepository.findById(request.carModelId())
                .orElseThrow(() -> new EntityNotFoundException("CarModel", request.carModelId()));

        Car car = new Car(
                UUID.randomUUID(),
                carModel,
                request.vin(),
                request.color(),
                request.price(),
                request.available(),
                request.availableForTestDrive()
        );

        return carMapper.toResponse(carRepository.save(car));
    }

    public SparePartResponse createSparePart(CreateSparePartRequest request) {
        validateCreateSparePartRequest(request);

        SparePart sparePart = new SparePart(
                UUID.randomUUID(),
                request.name(),
                request.description(),
                request.price(),
                request.compatibleCarModelIds()
        );

        return sparePartMapper.toResponse(sparePartRepository.save(sparePart));
    }

    public CarResponse getCarById(UUID carId) {
        return carRepository.findById(carId)
                .map(carMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Car", carId));
    }

    public SparePartResponse getSparePartById(UUID sparePartId) {
        return sparePartRepository.findById(sparePartId)
                .map(sparePartMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("SparePart", sparePartId));
    }

    public CarModelResponse getCarModelById(UUID carModelId) {
        return carModelRepository.findById(carModelId)
                .map(carModelMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("CarModel", carModelId));
    }

    public ComponentOptionResponse getComponentOptionById(UUID componentOptionId) {
        return componentOptionRepository.findById(componentOptionId)
                .map(componentOptionMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("ComponentOption", componentOptionId));
    }

    public List<CarResponse> getAllCars() {
        return carRepository.findAll().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public List<SparePartResponse> getAllSpareParts() {
        return sparePartRepository.findAll().stream()
                .map(sparePartMapper::toResponse)
                .toList();
    }

    public List<CarModelResponse> getAllCarModels() {
        return carModelRepository.findAll().stream()
                .map(carModelMapper::toResponse)
                .toList();
    }

    public List<ComponentOptionResponse> getAllComponentOptions() {
        return componentOptionRepository.findAll().stream()
                .map(componentOptionMapper::toResponse)
                .toList();
    }

    public CarResponse updateCar(UUID carId, UpdateCarRequest request) {
        if (request == null) {
            throw new DomainValidationException("Update car request must not be null");
        }

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car", carId));

        Optional.ofNullable(request.color())
                .ifPresent(car::updateColor);

        Optional.ofNullable(request.price())
                .ifPresent(car::updatePrice);

        Optional.ofNullable(request.available())
                .ifPresent(available -> {
                    if (available) {
                        car.markAsAvailable();
                    } else {
                        car.markAsUnavailable();
                    }
                });

        Optional.ofNullable(request.availableForTestDrive())
                .ifPresent(availableForTestDrive -> {
                    if (availableForTestDrive) {
                        car.enableTestDrive();
                    } else {
                        car.disableTestDrive();
                    }
                });

        return carMapper.toResponse(carRepository.save(car));
    }

    public SparePartResponse updateSparePart(UUID sparePartId, UpdateSparePartRequest request) {
        validateUpdateSparePartRequest(request);

        sparePartRepository.findById(sparePartId)
                .orElseThrow(() -> new EntityNotFoundException("SparePart", sparePartId));

        SparePart updatedSparePart = new SparePart(
                sparePartId,
                request.name(),
                request.description(),
                request.price(),
                request.compatibleCarModelIds()
        );

        return sparePartMapper.toResponse(sparePartRepository.save(updatedSparePart));
    }

    public void deleteCar(UUID carId) {
        if (!carRepository.existsById(carId)) {
            throw new EntityNotFoundException("Car", carId);
        }
        carRepository.deleteById(carId);
    }

    public void deleteSparePart(UUID sparePartId) {
        if (!sparePartRepository.existsById(sparePartId)) {
            throw new EntityNotFoundException("SparePart", sparePartId);
        }
        sparePartRepository.deleteById(sparePartId);
    }

    public void deleteCarModel(UUID carModelId) {
        if (!carModelRepository.existsById(carModelId)) {
            throw new EntityNotFoundException("CarModel", carModelId);
        }
        carModelRepository.deleteById(carModelId);
    }

    public void deleteComponentOption(UUID componentOptionId) {
        if (!componentOptionRepository.existsById(componentOptionId)) {
            throw new EntityNotFoundException("ComponentOption", componentOptionId);
        }
        componentOptionRepository.deleteById(componentOptionId);
    }

    private void validateCreateCarRequest(CreateCarRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create car request must not be null");
        }
    }

    private void validateCreateSparePartRequest(CreateSparePartRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create spare part request must not be null");
        }
    }

    private void validateUpdateSparePartRequest(UpdateSparePartRequest request) {
        if (request == null) {
            throw new DomainValidationException("Update spare part request must not be null");
        }
    }

    private void validateCreateComponentOptionRequest(CreateComponentOptionRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create component option request must not be null");
        }
    }

    private void validateCreateCarModelRequest(CreateCarModelRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create car model request must not be null");
        }
        if (request.id() == null) {
            throw new DomainValidationException("Car model id must not be null");
        }
        if (request.requiredComponentTypes() == null || request.requiredComponentTypes().isEmpty()) {
            throw new DomainValidationException("Required component types must not be null or empty");
        }
        if (request.baseComponentIds() == null || request.baseComponentIds().isEmpty()) {
            throw new DomainValidationException("Base component ids must not be null or empty");
        }
    }
}