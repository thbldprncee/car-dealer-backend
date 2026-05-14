package org.example.mappers;

import org.example.domain.model.ComponentOption;
import org.example.dto.response.ComponentOptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComponentOptionMapper {

    @Mapping(target = "componentType", source = "type")
    ComponentOptionResponse toResponse(ComponentOption componentOption);
}
