package org.example.mappers;

import org.example.domain.model.TestDriveRequest;
import org.example.dto.response.TestDriveRequestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TestDriveRequestMapper {

    TestDriveRequestMapper INSTANCE = Mappers.getMapper(TestDriveRequestMapper.class);

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(request.getClient().getFullName())")
    @Mapping(target = "carId", source = "car.id")
    @Mapping(target = "carDisplayName", expression = "java(request.getCar().getDisplayName())")
    TestDriveRequestResponse toResponse(TestDriveRequest request);
}