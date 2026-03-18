package org.example.mappers;

import org.example.domain.model.CustomCarOrder;
import org.example.dto.response.CustomCarOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CustomCarOrderMapper {

    CustomCarOrderMapper INSTANCE = Mappers.getMapper(CustomCarOrderMapper.class);

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(order.getClient().getFullName())")
    @Mapping(target = "salesManagerId", source = "salesManager.id")
    @Mapping(target = "salesManagerFullName", expression = "java(order.getSalesManager().getFullName())")
    @Mapping(target = "carModelId", source = "carModel.id")
    @Mapping(target = "carModelDisplayName", expression = "java(order.getCarModel().getDisplayName())")
    @Mapping(target = "carConfigurationId", source = "configuration.id")
    CustomCarOrderResponse toResponse(CustomCarOrder order);
}