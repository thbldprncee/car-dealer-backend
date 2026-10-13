package io.github.thbldprncee.cardealer.storage.mappers;

import io.github.thbldprncee.cardealer.storage.domain.model.CarModel;
import io.github.thbldprncee.cardealer.storage.dto.response.CarModelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ComponentOptionMapper.class)
public interface CarModelMapper {

    @Mapping(target = "displayName", expression = "java(carModel.getDisplayName())")
    CarModelResponse toResponse(CarModel carModel);
}
