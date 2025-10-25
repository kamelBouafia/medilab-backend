package com.medilab.service;

import com.medilab.entity.TestResult;
import com.medilab.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestResultService {

    private final TestResultRepository testResultRepository;

    @Autowired
    public TestResultService(TestResultRepository testResultRepository) {
        this.testResultRepository = testResultRepository;
    }

    public void saveTestResults(List<TestResult> testResults) {
        // Add any business logic before saving
        testResultRepository.saveAll(testResults);
    }
}
