package com.medilab.controller;

import com.medilab.dto.BulkInterpretationRequestDto;
import com.medilab.dto.BulkInterpretationResponseDto;
import com.medilab.service.InterpretationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interpretations")
@PreAuthorize("hasRole('Staff')")
@RequiredArgsConstructor
public class InterpretationController {

    private final InterpretationService interpretationService;

    @PostMapping("/bulk")
    public ResponseEntity<BulkInterpretationResponseDto> getBulkInterpretations(@RequestBody BulkInterpretationRequestDto request) {
        BulkInterpretationResponseDto response = interpretationService.getBulkInterpretations(request);
        return ResponseEntity.ok(response);
    }
}
