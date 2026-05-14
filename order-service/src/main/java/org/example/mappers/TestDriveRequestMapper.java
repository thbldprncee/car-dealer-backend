package org.example.mappers;

import org.example.domain.model.TestDriveRequest;
import org.example.dto.response.TestDriveRequestResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestDriveRequestMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(request.getClient().getFullName())")
    TestDriveRequestResponse toResponse(TestDriveRequest request);
}
