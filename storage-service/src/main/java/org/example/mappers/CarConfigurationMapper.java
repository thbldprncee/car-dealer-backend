package org.example.mappers;

import org.example.domain.model.CarConfiguration;
import org.example.dto.response.CarConfigurationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ComponentOptionMapper.class)
public interface CarConfigurationMapper {

    @Mapping(target = "carModelId", source = "carModel.id")
    @Mapping(target = "carModelDisplayName", expression = "java(configuration.getCarModel().getDisplayName())")
    @Mapping(target = "totalPrice", expression = "java(configuration.calculateTotalPrice())")
    CarConfigurationResponse toResponse(CarConfiguration configuration);
}
