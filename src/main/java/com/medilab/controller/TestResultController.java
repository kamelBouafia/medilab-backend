package com.medilab.controller;

import com.medilab.dto.TestResultDto;
import com.medilab.service.TestResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-results")
@PreAuthorize("hasRole('Staff')")
@RequiredArgsConstructor
public class TestResultController {

    private final TestResultService testResultService;

    @PostMapping
    public ResponseEntity<?> saveTestResults(@Valid @RequestBody List<TestResultDto> testResultDtos) {
        testResultService.saveTestResults(testResultDtos);
        return new ResponseEntity<>(java.util.Map.of("message", "Results saved successfully"), HttpStatus.CREATED);
    }
}
