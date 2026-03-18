package org.example.mappers;

import org.example.domain.model.SparePart;
import org.example.dto.response.SparePartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SparePartMapper {

    SparePartMapper INSTANCE = Mappers.getMapper(SparePartMapper.class);

    SparePartResponse toResponse(SparePart sparePart);
}