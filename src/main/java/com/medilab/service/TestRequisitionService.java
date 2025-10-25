package com.medilab.service;

import com.medilab.entity.RequisitionTest;
import com.medilab.repository.TestRequisitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestRequisitionService {

    private final TestRequisitionRepository testRequisitionRepository;

    @Autowired
    public TestRequisitionService(TestRequisitionRepository testRequisitionRepository) {
        this.testRequisitionRepository = testRequisitionRepository;
    }

    public List<RequisitionTest> getAllRequisitions() {
        return testRequisitionRepository.findAll();
    }

    public RequisitionTest addRequisition(RequisitionTest testRequisition) {
        return testRequisitionRepository.save(testRequisition);
    }
}
