package com.medilab.mapper;

import com.medilab.dto.InventoryItemDto;
import com.medilab.entity.InventoryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryItemMapper {

    @Mapping(source = "addedBy.id", target = "addedById")
    InventoryItemDto toDto(InventoryItem inventoryItem);

    @Mapping(target = "addedBy", ignore = true)
    @Mapping(target = "lab", ignore = true)
    InventoryItem toEntity(InventoryItemDto dto);
}
