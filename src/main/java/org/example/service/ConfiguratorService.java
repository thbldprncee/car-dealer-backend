package org.example.service;

import org.example.domain.enums.ComponentType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.exceptions.IncompatibleComponentException;
import org.example.domain.model.CarConfiguration;
import org.example.domain.model.CarModel;
import org.example.domain.model.ComponentOption;
import org.example.dto.request.BuildCarConfigurationRequest;
import org.example.dto.response.CarConfigurationResponse;
import org.example.mappers.CarConfigurationMapper;
import org.example.repository.CarConfigurationRepository;
import org.example.repository.CarModelRepository;
import org.example.repository.ComponentOptionRepository;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ConfiguratorService {

    private final CarModelRepository carModelRepository;
    private final ComponentOptionRepository componentOptionRepository;
    private final CarConfigurationRepository carConfigurationRepository;
    private final CarConfigurationMapper carConfigurationMapper;

    public ConfiguratorService(
            CarModelRepository carModelRepository,
            ComponentOptionRepository componentOptionRepository,
            CarConfigurationRepository carConfigurationRepository
    ) {
        this.carModelRepository = carModelRepository;
        this.componentOptionRepository = componentOptionRepository;
        this.carConfigurationRepository = carConfigurationRepository;
        this.carConfigurationMapper = CarConfigurationMapper.INSTANCE;
    }

    public CarConfigurationResponse buildConfiguration(BuildCarConfigurationRequest request) {
        validateBuildRequest(request);

        CarModel carModel = carModelRepository.findById(request.carModelId())
                .orElseThrow(() -> new EntityNotFoundException("CarModel", request.carModelId()));

        Map<ComponentType, UUID> selectedComponentOptionIds = Optional.ofNullable(request.selectedComponentOptionIds())
                .filter(map -> !map.isEmpty())
                .orElseThrow(() -> new DomainValidationException("Selected component option ids must not be null or empty"));

        validateRequiredComponents(carModel, selectedComponentOptionIds);

        Map<ComponentType, ComponentOption> selectedComponents = new EnumMap<>(ComponentType.class);

        selectedComponentOptionIds.forEach((componentType, componentOptionId) -> {
            ComponentType validatedComponentType = Optional.ofNullable(componentType)
                    .orElseThrow(() -> new DomainValidationException("Component type must not be null"));

            UUID validatedComponentOptionId = Optional.ofNullable(componentOptionId)
                    .orElseThrow(() -> new DomainValidationException("Component option id must not be null"));

            ComponentOption componentOption = componentOptionRepository.findById(validatedComponentOptionId)
                    .orElseThrow(() -> new EntityNotFoundException("ComponentOption", validatedComponentOptionId));

            if (componentOption.componentType() != validatedComponentType) {
                throw new DomainValidationException(
                        "Component option type does not match selected component type " + validatedComponentType
                );
            }

            if (!componentOption.isCompatibleWith(carModel)) {
                throw new IncompatibleComponentException(
                        "Selected component '%s' is incompatible with car model '%s'"
                                .formatted(componentOption.name(), carModel.getDisplayName())
                );
            }

            selectedComponents.put(validatedComponentType, componentOption);
        });

        CarConfiguration configuration = new CarConfiguration(
                UUID.randomUUID(),
                carModel,
                selectedComponents
        );

        return carConfigurationMapper.toResponse(carConfigurationRepository.save(configuration));
    }

    public CarConfigurationResponse getConfigurationById(UUID id) {
        return carConfigurationRepository.findById(id)
                .map(carConfigurationMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("CarConfiguration", id));
    }

    private void validateBuildRequest(BuildCarConfigurationRequest request) {
        if (request == null) {
            throw new DomainValidationException("Build car configuration request must not be null");
        }
    }

    private void validateRequiredComponents(
            CarModel carModel,
            Map<ComponentType, UUID> selectedComponentOptionIds
    ) {
        carModel.getRequiredComponentTypes().forEach(requiredComponentType -> {
            if (!selectedComponentOptionIds.containsKey(requiredComponentType)
                    || selectedComponentOptionIds.get(requiredComponentType) == null) {
                throw new DomainValidationException(
                        "Required component '%s' is missing".formatted(requiredComponentType)
                );
            }
        });
    }
}