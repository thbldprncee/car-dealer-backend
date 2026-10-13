package io.github.thbldprncee.cardealer.storage.service;

import lombok.RequiredArgsConstructor;
import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;
import io.github.thbldprncee.cardealer.storage.domain.exceptions.DomainValidationException;
import io.github.thbldprncee.cardealer.storage.domain.exceptions.EntityNotFoundException;
import io.github.thbldprncee.cardealer.storage.domain.model.Car;
import io.github.thbldprncee.cardealer.storage.domain.model.CarModel;
import io.github.thbldprncee.cardealer.storage.domain.model.ComponentOption;
import io.github.thbldprncee.cardealer.storage.domain.model.SparePart;
import io.github.thbldprncee.cardealer.storage.dto.request.CarModelFilterRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateCarModelRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateCarRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateComponentOptionRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateSparePartRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.UpdateCarRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.UpdateSparePartRequest;
import io.github.thbldprncee.cardealer.storage.dto.response.CarModelResponse;
import io.github.thbldprncee.cardealer.storage.dto.response.CarResponse;
import io.github.thbldprncee.cardealer.storage.dto.response.ComponentOptionResponse;
import io.github.thbldprncee.cardealer.storage.dto.response.SparePartResponse;
import io.github.thbldprncee.cardealer.storage.mappers.CarMapper;
import io.github.thbldprncee.cardealer.storage.mappers.CarModelMapper;
import io.github.thbldprncee.cardealer.storage.mappers.ComponentOptionMapper;
import io.github.thbldprncee.cardealer.storage.mappers.SparePartMapper;
import io.github.thbldprncee.cardealer.storage.repository.CarModelRepository;
import io.github.thbldprncee.cardealer.storage.repository.CarRepository;
import io.github.thbldprncee.cardealer.storage.repository.ComponentOptionRepository;
import io.github.thbldprncee.cardealer.storage.repository.SparePartRepository;
import io.github.thbldprncee.cardealer.storage.repository.specification.CarModelSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final SparePartRepository sparePartRepository;
    private final ComponentOptionRepository componentOptionRepository;

    private final CarMapper carMapper;
    private final CarModelMapper carModelMapper;
    private final SparePartMapper sparePartMapper;
    private final ComponentOptionMapper componentOptionMapper;

    @Transactional
    public CarModelResponse createCarModel(CreateCarModelRequest request) {
        validateCreateCarModelRequest(request);

        carModelRepository.findByBrandIgnoreCaseAndModelNameIgnoreCaseAndRemovedFalse(
                request.brand(),
                request.modelName()
        ).ifPresent(existingModel -> {
            throw new DomainValidationException(
                    "Car model %s %s already exists".formatted(request.brand(), request.modelName())
            );
        });

        Map<ComponentType, ComponentOption> baseComponents = new EnumMap<>(ComponentType.class);

        request.baseComponentIds().forEach((componentType, componentId) -> {
            ComponentOption componentOption = componentOptionRepository.findByIdAndRemovedFalse(componentId)
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

    @Transactional
    public ComponentOptionResponse createComponentOption(CreateComponentOptionRequest request) {
        validateCreateComponentOptionRequest(request);

        componentOptionRepository.findByNameIgnoreCaseAndRemovedFalse(request.name())
                .ifPresent(existingComponent -> {
                    throw new DomainValidationException(
                            "Component option with name=%s already exists".formatted(request.name())
                    );
                });

        ComponentOption componentOption = new ComponentOption(
                UUID.randomUUID(),
                request.componentType(),
                request.name(),
                request.priceDelta(),
                request.compatibleCarModelIds()
        );

        return componentOptionMapper.toResponse(componentOptionRepository.save(componentOption));
    }

    @Transactional
    public CarResponse createCar(CreateCarRequest request) {
        validateCreateCarRequest(request);

        carRepository.findByVinAndRemovedFalse(request.vin())
                .ifPresent(existingCar -> {
                    throw new DomainValidationException(
                            "Car with vin=%s already exists".formatted(request.vin())
                    );
                });

        CarModel carModel = carModelRepository.findByIdAndRemovedFalse(request.carModelId())
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

    @Transactional
    public SparePartResponse createSparePart(CreateSparePartRequest request) {
        validateCreateSparePartRequest(request);

        sparePartRepository.findByNameIgnoreCaseAndRemovedFalse(request.name())
                .ifPresent(existingSparePart -> {
                    throw new DomainValidationException(
                            "Spare part with name=%s already exists".formatted(request.name())
                    );
                });

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
        return carRepository.findByIdAndRemovedFalse(carId)
                .map(carMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Car", carId));
    }

    public SparePartResponse getSparePartById(UUID sparePartId) {
        return sparePartRepository.findByIdAndRemovedFalse(sparePartId)
                .map(sparePartMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("SparePart", sparePartId));
    }

    public CarModelResponse getCarModelById(UUID carModelId) {
        return carModelRepository.findByIdAndRemovedFalse(carModelId)
                .map(carModelMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("CarModel", carModelId));
    }

    public ComponentOptionResponse getComponentOptionById(UUID componentOptionId) {
        return componentOptionRepository.findByIdAndRemovedFalse(componentOptionId)
                .map(componentOptionMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("ComponentOption", componentOptionId));
    }

    public List<CarResponse> getAllCars() {
        return carRepository.findAllByRemovedFalse().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public List<SparePartResponse> getAllSpareParts() {
        return sparePartRepository.findAllByRemovedFalse().stream()
                .map(sparePartMapper::toResponse)
                .toList();
    }

    public List<CarModelResponse> getAllCarModels() {
        return carModelRepository.findAllByRemovedFalse().stream()
                .map(carModelMapper::toResponse)
                .toList();
    }

    public List<ComponentOptionResponse> getAllComponentOptions() {
        return componentOptionRepository.findAllByRemovedFalse().stream()
                .map(componentOptionMapper::toResponse)
                .toList();
    }

    public List<CarModelResponse> filterBaseCarConfigurations(CarModelFilterRequest request) {
        Specification<CarModel> specification = CarModelSpecification.notRemoved()
                .and(CarModelSpecification.hasBrand(request == null ? null : request.brand()))
                .and(CarModelSpecification.hasAllBaseComponents(
                        request == null ? null : request.componentOptionIds()
                ));

        return carModelRepository.findAll(specification).stream()
                .map(carModelMapper::toResponse)
                .toList();
    }

    @Transactional
    public CarResponse updateCar(UUID carId, UpdateCarRequest request) {
        validateUpdateCarRequest(request);

        Car car = carRepository.findByIdAndRemovedFalse(carId)
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

    @Transactional
    public SparePartResponse updateSparePart(UUID sparePartId, UpdateSparePartRequest request) {
        validateUpdateSparePartRequest(request);

        SparePart sparePart = sparePartRepository.findByIdAndRemovedFalse(sparePartId)
                .orElseThrow(() -> new EntityNotFoundException("SparePart", sparePartId));

        sparePart.update(
                request.name(),
                request.description(),
                request.price(),
                request.compatibleCarModelIds()
        );

        return sparePartMapper.toResponse(sparePartRepository.save(sparePart));
    }

    @Transactional
    public void deleteCar(UUID carId) {
        Car car = carRepository.findByIdAndRemovedFalse(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car", carId));

        car.markAsRemoved();
        carRepository.save(car);
    }

    @Transactional
    public void deleteSparePart(UUID sparePartId) {
        SparePart sparePart = sparePartRepository.findByIdAndRemovedFalse(sparePartId)
                .orElseThrow(() -> new EntityNotFoundException("SparePart", sparePartId));

        sparePart.markAsRemoved();
        sparePartRepository.save(sparePart);
    }

    @Transactional
    public void deleteCarModel(UUID carModelId) {
        CarModel carModel = carModelRepository.findByIdAndRemovedFalse(carModelId)
                .orElseThrow(() -> new EntityNotFoundException("CarModel", carModelId));

        carModel.markAsRemoved();
        carModelRepository.save(carModel);
    }

    @Transactional
    public void deleteComponentOption(UUID componentOptionId) {
        ComponentOption componentOption = componentOptionRepository.findByIdAndRemovedFalse(componentOptionId)
                .orElseThrow(() -> new EntityNotFoundException("ComponentOption", componentOptionId));

        componentOption.markAsRemoved();
        componentOptionRepository.save(componentOption);
    }

    private void validateCreateCarRequest(CreateCarRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create car request must not be null");
        }

        if (request.carModelId() == null) {
            throw new DomainValidationException("Car model id must not be null");
        }
    }

    private void validateUpdateCarRequest(UpdateCarRequest request) {
        if (request == null) {
            throw new DomainValidationException("Update car request must not be null");
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
