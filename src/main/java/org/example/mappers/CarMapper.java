package org.example.mappers;

import org.example.domain.model.Car;
import org.example.dto.response.CarResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CarMapper {

    CarMapper INSTANCE = Mappers.getMapper(CarMapper.class);

    @Mapping(target = "carModelId", source = "carModel.id")
    @Mapping(target = "carModelDisplayName", expression = "java(car.getCarModel().getDisplayName())")
    CarResponse toResponse(Car car);
}