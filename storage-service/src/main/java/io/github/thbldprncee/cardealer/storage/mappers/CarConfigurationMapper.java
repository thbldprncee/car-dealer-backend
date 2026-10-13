package io.github.thbldprncee.cardealer.storage.mappers;

import io.github.thbldprncee.cardealer.storage.domain.model.CarConfiguration;
import io.github.thbldprncee.cardealer.storage.dto.response.CarConfigurationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ComponentOptionMapper.class)
public interface CarConfigurationMapper {

    @Mapping(target = "carModelId", source = "carModel.id")
    @Mapping(target = "carModelDisplayName", expression = "java(configuration.getCarModel().getDisplayName())")
    @Mapping(target = "totalPrice", expression = "java(configuration.calculateTotalPrice())")
    CarConfigurationResponse toResponse(CarConfiguration configuration);
}
