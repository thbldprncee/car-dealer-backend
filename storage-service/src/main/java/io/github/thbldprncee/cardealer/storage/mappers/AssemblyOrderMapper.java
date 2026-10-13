package io.github.thbldprncee.cardealer.storage.mappers;

import io.github.thbldprncee.cardealer.storage.domain.model.AssemblyOrder;
import io.github.thbldprncee.cardealer.storage.dto.response.AssemblyOrderResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssemblyOrderMapper {

    AssemblyOrderResponse toResponse(AssemblyOrder assemblyOrder);
}
