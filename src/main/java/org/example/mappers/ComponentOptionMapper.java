package org.example.mappers;

import org.example.domain.model.ComponentOption;
import org.example.dto.response.ComponentOptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ComponentOptionMapper {

    ComponentOptionMapper INSTANCE = Mappers.getMapper(ComponentOptionMapper.class);

    ComponentOptionResponse toResponse(ComponentOption componentOption);
}