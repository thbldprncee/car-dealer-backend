package io.github.thbldprncee.cardealer.storage.mappers;

import io.github.thbldprncee.cardealer.storage.domain.model.Car;
import io.github.thbldprncee.cardealer.storage.dto.response.CarResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarMapper {

    @Mapping(target = "carModelId", source = "carModel.id")
    @Mapping(target = "carModelDisplayName", expression = "java(car.getCarModel().getDisplayName())")
    CarResponse toResponse(Car car);
}
