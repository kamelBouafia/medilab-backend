package com.medilab.mapper;

import com.medilab.dto.InterLabAgreementDto;
import com.medilab.entity.InterLabAgreement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { AgreementTestPriceMapper.class })
public interface InterLabAgreementMapper {

    @Mapping(source = "mainLab.id", target = "mainLabId")
    @Mapping(source = "mainLab.name", target = "mainLabName")
    @Mapping(source = "partnerLab.id", target = "partnerLabId")
    @Mapping(source = "partnerLab.name", target = "partnerLabName")
    @Mapping(source = "requestedBy.id", target = "requestedById")
    @Mapping(source = "requestedBy.name", target = "requestedByName")
    @Mapping(source = "reviewedBy.id", target = "reviewedById")
    @Mapping(source = "reviewedBy.name", target = "reviewedByName")
    InterLabAgreementDto toDto(InterLabAgreement entity);

    List<InterLabAgreementDto> toDtoList(List<InterLabAgreement> entities);

    @Mapping(target = "mainLab", ignore = true)
    @Mapping(target = "partnerLab", ignore = true)
    @Mapping(target = "requestedBy", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "testPrices", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    InterLabAgreement toEntity(InterLabAgreementDto dto);
}
