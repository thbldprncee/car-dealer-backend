package io.github.thbldprncee.cardealer.storage.service;

import lombok.RequiredArgsConstructor;
import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;
import io.github.thbldprncee.cardealer.storage.domain.exceptions.DomainValidationException;
import io.github.thbldprncee.cardealer.storage.domain.exceptions.EntityNotFoundException;
import io.github.thbldprncee.cardealer.storage.domain.exceptions.IncompatibleComponentException;
import io.github.thbldprncee.cardealer.storage.domain.model.CarConfiguration;
import io.github.thbldprncee.cardealer.storage.domain.model.CarModel;
import io.github.thbldprncee.cardealer.storage.domain.model.ComponentOption;
import io.github.thbldprncee.cardealer.storage.dto.request.BuildCarConfigurationRequest;
import io.github.thbldprncee.cardealer.storage.dto.response.CarConfigurationResponse;
import io.github.thbldprncee.cardealer.storage.mappers.CarConfigurationMapper;
import io.github.thbldprncee.cardealer.storage.repository.CarConfigurationRepository;
import io.github.thbldprncee.cardealer.storage.repository.CarModelRepository;
import io.github.thbldprncee.cardealer.storage.repository.ComponentOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguratorService {

    private final CarModelRepository carModelRepository;
    private final ComponentOptionRepository componentOptionRepository;
    private final CarConfigurationRepository carConfigurationRepository;

    private final CarConfigurationMapper carConfigurationMapper;

    @Transactional
    public CarConfigurationResponse buildConfiguration(BuildCarConfigurationRequest request) {
        validateRequest(request);

        CarModel carModel = carModelRepository.findByIdAndRemovedFalse(request.carModelId())
                .orElseThrow(() -> new EntityNotFoundException("CarModel", request.carModelId()));

        Map<ComponentType, ComponentOption> selectedComponents = new EnumMap<>(ComponentType.class);

        request.selectedComponentOptionIds().forEach((componentType, componentOptionId) -> {
            ComponentType validatedComponentType = validateComponentType(componentType);
            UUID validatedComponentOptionId = validateComponentOptionId(componentOptionId);

            ComponentOption componentOption = componentOptionRepository.findByIdAndRemovedFalse(validatedComponentOptionId)
                    .orElseThrow(() -> new EntityNotFoundException("ComponentOption", validatedComponentOptionId));

            validateComponentTypeMatch(componentOption, validatedComponentType);
            validateComponentCompatibility(componentOption, carModel);

            selectedComponents.put(validatedComponentType, componentOption);
        });

        CarConfiguration configuration = new CarConfiguration(
                UUID.randomUUID(),
                carModel,
                selectedComponents
        );

        return carConfigurationMapper.toResponse(carConfigurationRepository.save(configuration));
    }

    public CarConfigurationResponse getConfigurationById(UUID configurationId) {
        return carConfigurationRepository.findByIdAndRemovedFalse(configurationId)
                .map(carConfigurationMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("CarConfiguration", configurationId));
    }

    private void validateRequest(BuildCarConfigurationRequest request) {
        if (request == null) {
            throw new DomainValidationException("Build car configuration request must not be null");
        }

        if (request.carModelId() == null) {
            throw new DomainValidationException("Car model id must not be null");
        }

        if (request.selectedComponentOptionIds() == null || request.selectedComponentOptionIds().isEmpty()) {
            throw new DomainValidationException("Selected component option ids must not be null or empty");
        }
    }

    private ComponentType validateComponentType(ComponentType componentType) {
        if (componentType == null) {
            throw new DomainValidationException("Component type must not be null");
        }

        return componentType;
    }

    private UUID validateComponentOptionId(UUID componentOptionId) {
        if (componentOptionId == null) {
            throw new DomainValidationException("Component option id must not be null");
        }

        return componentOptionId;
    }

    private void validateComponentTypeMatch(
            ComponentOption componentOption,
            ComponentType selectedComponentType
    ) {
        if (componentOption.getType() != selectedComponentType) {
            throw new DomainValidationException(
                    "Selected component option type does not match requested component type"
            );
        }
    }

    private void validateComponentCompatibility(
            ComponentOption componentOption,
            CarModel carModel
    ) {
        if (!componentOption.isCompatibleWith(carModel)) {
            throw new IncompatibleComponentException(
                    "Component '%s' is incompatible with car model '%s'"
                            .formatted(componentOption.getName(), carModel.getDisplayName())
            );
        }
    }
}
