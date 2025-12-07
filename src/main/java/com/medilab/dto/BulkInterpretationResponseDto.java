package com.medilab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkInterpretationResponseDto {
    private List<InterpretationDto> interpretations;
}
