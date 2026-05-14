package org.example.mappers;

import org.example.domain.model.SparePart;
import org.example.dto.response.SparePartResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SparePartMapper {

    SparePartResponse toResponse(SparePart sparePart);
}
