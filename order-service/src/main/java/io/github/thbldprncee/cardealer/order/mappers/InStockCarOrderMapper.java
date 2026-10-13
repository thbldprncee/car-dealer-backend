package io.github.thbldprncee.cardealer.order.mappers;

import io.github.thbldprncee.cardealer.order.domain.model.InStockCarOrder;
import io.github.thbldprncee.cardealer.order.dto.response.InStockCarOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InStockCarOrderMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(order.getClient().getFullName())")
    @Mapping(target = "salesManagerId", source = "salesManager.id")
    @Mapping(target = "salesManagerFullName", expression = "java(order.getSalesManager().getFullName())")
    InStockCarOrderResponse toResponse(InStockCarOrder order);
}
