package org.example.mappers;

import org.example.domain.model.CustomCarOrder;
import org.example.dto.response.CustomCarOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomCarOrderMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(order.getClient().getFullName())")
    @Mapping(target = "salesManagerId", source = "salesManager.id")
    @Mapping(target = "salesManagerFullName", expression = "java(order.getSalesManager().getFullName())")
    CustomCarOrderResponse toResponse(CustomCarOrder order);
}
