package org.example.mappers;

import org.example.domain.model.CarConfiguration;
import org.example.dto.response.CarConfigurationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = ComponentOptionMapper.class)
public interface CarConfigurationMapper {

    CarConfigurationMapper INSTANCE = Mappers.getMapper(CarConfigurationMapper.class);

    @Mapping(target = "carModelId", source = "carModel.id")
    @Mapping(target = "carModelDisplayName", expression = "java(configuration.getCarModel().getDisplayName())")
    @Mapping(target = "totalPrice", expression = "java(configuration.calculateTotalPrice())")
    CarConfigurationResponse toResponse(CarConfiguration configuration);
}