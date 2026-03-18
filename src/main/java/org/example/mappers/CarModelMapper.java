package org.example.mappers;

import org.example.domain.model.CarModel;
import org.example.dto.response.CarModelResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = ComponentOptionMapper.class)
public interface CarModelMapper {

    CarModelMapper INSTANCE = Mappers.getMapper(CarModelMapper.class);

    @Mapping(target = "displayName", expression = "java(carModel.getDisplayName())")
    CarModelResponse toResponse(CarModel carModel);
}