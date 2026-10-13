package io.github.thbldprncee.cardealer.storage.mappers;

import io.github.thbldprncee.cardealer.storage.domain.model.ComponentOption;
import io.github.thbldprncee.cardealer.storage.dto.response.ComponentOptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComponentOptionMapper {

    @Mapping(target = "componentType", source = "type")
    ComponentOptionResponse toResponse(ComponentOption componentOption);
}
