package com.medilab.dto;

import lombok.Data;

import java.util.List;

@Data
public class BulkInterpretationRequestDto {
    private List<InterpretationInputDto> results;
}
