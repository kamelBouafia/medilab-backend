package com.medilab.mapper;

import com.medilab.dto.GlobalTestCatalogDto;
import com.medilab.entity.GlobalTestCatalog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GlobalTestCatalogMapper {
    GlobalTestCatalogDto toDto(GlobalTestCatalog entity);

    GlobalTestCatalog toEntity(GlobalTestCatalogDto dto);
}
