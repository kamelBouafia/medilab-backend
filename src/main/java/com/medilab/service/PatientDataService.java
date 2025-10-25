package com.medilab.service;

import com.medilab.entity.RequisitionTest;
import com.medilab.entity.TestResult;
import com.medilab.repository.TestRequisitionRepository;
import com.medilab.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientDataService {

    private final TestRequisitionRepository testRequisitionRepository;
    private final TestResultRepository testResultRepository;

    @Autowired
    public PatientDataService(TestRequisitionRepository testRequisitionRepository, TestResultRepository testResultRepository) {
        this.testRequisitionRepository = testRequisitionRepository;
        this.testResultRepository = testResultRepository;
    }

    public List<RequisitionTest> getRequisitionsForPatient() {
        // In a real app, you'd get the current patient's ID from the security context
        // and filter requisitions by that ID.
        return testRequisitionRepository.findAll();
    }

    public List<TestResult> getTestResults(String requisitionId) {
        // You would need a custom query in your TestResultRepository to find by requisitionId
        // For now, returning all results for demonstration
        return testResultRepository.findAll();
    }
}
