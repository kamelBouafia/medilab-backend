package com.medilab.controller;

import com.medilab.dto.TestResultDto;
import com.medilab.service.AuditLogService;
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
    private final AuditLogService auditLogService;

    @PostMapping
    public ResponseEntity<List<TestResultDto>> saveTestResults(@Valid @RequestBody List<TestResultDto> testResultDtos) {
        List<TestResultDto> savedResults = testResultService.saveTestResults(testResultDtos);
        auditLogService.logAction("ENTER_RESULTS", "Entered " + testResultDtos.size() + " test results");
        return new ResponseEntity<>(savedResults, HttpStatus.CREATED);
    }
}
