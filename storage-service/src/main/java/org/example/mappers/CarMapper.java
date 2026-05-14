package org.example.mappers;

import org.example.domain.model.Car;
import org.example.dto.response.CarResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarMapper {

    @Mapping(target = "carModelId", source = "carModel.id")
    @Mapping(target = "carModelDisplayName", expression = "java(car.getCarModel().getDisplayName())")
    CarResponse toResponse(Car car);
}
