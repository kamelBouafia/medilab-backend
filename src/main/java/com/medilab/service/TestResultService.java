package com.medilab.service;

import com.medilab.dto.TestResultDto;
import com.medilab.entity.SampleStatus;
import com.medilab.entity.StaffUser;
import com.medilab.entity.TestResult;
import com.medilab.mapper.TestResultMapper;
import com.medilab.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestResultService {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private TestResultMapper testResultMapper;

    public List<TestResultDto> saveTestResults(List<TestResultDto> testResultDtos) {
        StaffUser user = (StaffUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<TestResult> testResults = testResultDtos.stream().map(dto -> {
            TestResult testResult = testResultMapper.toEntity(dto);

            requisitionRepository.findById(dto.getRequisitionId()).ifPresent(testResult::setRequisition);
            labTestRepository.findById(dto.getTestId()).ifPresent(testResult::setTest);
            staffUserRepository.findById(user.getId()).ifPresent(testResult::setEnteredBy);
            labRepository.findById(user.getLab().getId()).ifPresent(testResult::setLab);

            return testResult;
        }).collect(Collectors.toList());

        List<TestResult> savedTestResults = testResultRepository.saveAll(testResults);

        // Update requisition status to 'Completed'
        if (!savedTestResults.isEmpty()) {
            savedTestResults.stream()
                .map(TestResult::getRequisition)
                .distinct()
                .forEach(requisition -> {
                    requisition.setStatus(SampleStatus.COMPLETED);
                    requisitionRepository.save(requisition);
                });
        }

        return savedTestResults.stream()
                .map(testResultMapper::toDto)
                .collect(Collectors.toList());
    }
}
