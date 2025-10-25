package com.medilab.service;

import com.medilab.entity.LabTest;
import com.medilab.repository.LabTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LabTestService {

    private final LabTestRepository labTestRepository;

    @Autowired
    public LabTestService(LabTestRepository labTestRepository) {
        this.labTestRepository = labTestRepository;
    }

    public List<LabTest> getAllLabTests() {
        return labTestRepository.findAll();
    }
}
