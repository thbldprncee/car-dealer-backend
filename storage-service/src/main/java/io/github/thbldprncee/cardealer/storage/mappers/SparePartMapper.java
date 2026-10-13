package io.github.thbldprncee.cardealer.storage.mappers;

import io.github.thbldprncee.cardealer.storage.domain.model.SparePart;
import io.github.thbldprncee.cardealer.storage.dto.response.SparePartResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SparePartMapper {

    SparePartResponse toResponse(SparePart sparePart);
}
