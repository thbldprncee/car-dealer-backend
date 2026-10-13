package io.github.thbldprncee.cardealer.order.mappers;

import io.github.thbldprncee.cardealer.order.domain.model.TestDriveRequest;
import io.github.thbldprncee.cardealer.order.dto.response.TestDriveRequestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestDriveRequestMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(request.getClient().getFullName())")
    TestDriveRequestResponse toResponse(TestDriveRequest request);
}
