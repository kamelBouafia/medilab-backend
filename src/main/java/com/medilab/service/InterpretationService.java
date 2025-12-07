package com.medilab.service;

import com.medilab.dto.BulkInterpretationRequestDto;
import com.medilab.dto.BulkInterpretationResponseDto;
import com.medilab.dto.InterpretationDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterpretationService {

    public BulkInterpretationResponseDto getBulkInterpretations(BulkInterpretationRequestDto request) {
        List<InterpretationDto> interpretations = request.getResults().stream()
                .map(result -> new InterpretationDto(result.getTestName(), "Within normal range."))
                .collect(Collectors.toList());
        return new BulkInterpretationResponseDto(interpretations);
    }
}
