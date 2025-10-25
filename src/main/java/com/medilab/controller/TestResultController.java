package com.medilab.controller;

import com.medilab.dto.TestResultDto;
import com.medilab.service.TestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/results")
@PreAuthorize("hasRole('Staff')")
public class TestResultController {

    @Autowired
    private TestResultService testResultService;

    @PostMapping
    public ResponseEntity<?> saveTestResults(@RequestBody List<TestResultDto> testResultDtos) {
        testResultService.saveTestResults(testResultDtos);
        return new ResponseEntity<>(Map.of("message", "Results saved successfully"), HttpStatus.CREATED);
    }
}
