package com.medilab.mapper;

import com.medilab.dto.AgreementTestPriceDto;
import com.medilab.entity.AgreementTestPrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgreementTestPriceMapper {

    @Mapping(source = "labTest.id", target = "testId")
    @Mapping(source = "labTest.name", target = "testName")
    @Mapping(target = "testCategory", ignore = true)
    AgreementTestPriceDto toDto(AgreementTestPrice entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agreement", ignore = true)
    @Mapping(target = "labTest", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AgreementTestPrice toEntity(AgreementTestPriceDto dto);
}
