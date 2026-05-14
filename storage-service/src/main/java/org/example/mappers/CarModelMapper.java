package org.example.mappers;

import org.example.domain.model.CarModel;
import org.example.dto.response.CarModelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ComponentOptionMapper.class)
public interface CarModelMapper {

    @Mapping(target = "displayName", expression = "java(carModel.getDisplayName())")
    CarModelResponse toResponse(CarModel carModel);
}
