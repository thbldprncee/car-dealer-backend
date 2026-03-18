package org.example.mappers;

import org.example.domain.model.InStockCarOrder;
import org.example.dto.response.InStockCarOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InStockCarOrderMapper {

    InStockCarOrderMapper INSTANCE = Mappers.getMapper(InStockCarOrderMapper.class);

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(order.getClient().getFullName())")
    @Mapping(target = "salesManagerId", source = "salesManager.id")
    @Mapping(target = "salesManagerFullName", expression = "java(order.getSalesManager().getFullName())")
    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "carDisplayName", expression = "java(order.getCar().getDisplayName())")
    InStockCarOrderResponse toResponse(InStockCarOrder order);
}