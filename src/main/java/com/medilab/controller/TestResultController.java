package com.medilab.controller;

import com.medilab.entity.TestResult;
import com.medilab.service.TestResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class TestResultController {

    private final TestResultService testResultService;

    @Autowired
    public TestResultController(TestResultService testResultService) {
        this.testResultService = testResultService;
    }

    @PostMapping
    public ResponseEntity<Void> saveTestResults(@RequestBody List<TestResult> testResults) {
        testResultService.saveTestResults(testResults);
        return ResponseEntity.ok().build();
    }
}
