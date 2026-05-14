package org.example.mappers;

import org.example.domain.model.AssemblyOrder;
import org.example.dto.response.AssemblyOrderResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssemblyOrderMapper {

    AssemblyOrderResponse toResponse(AssemblyOrder assemblyOrder);
}
