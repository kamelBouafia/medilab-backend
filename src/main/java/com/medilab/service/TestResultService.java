package com.medilab.service;

import com.medilab.dto.TestResultDto;
import com.medilab.entity.Requisition;
import com.medilab.entity.SampleStatus;
import com.medilab.entity.TestResult;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.TestResultMapper;
import com.medilab.repository.*;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final RequisitionRepository requisitionRepository;
    private final LabTestRepository labTestRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final TestResultMapper testResultMapper;

    @Transactional
    public List<TestResultDto> saveTestResults(List<TestResultDto> testResultDtos) {
        if (testResultDtos.isEmpty()) {
            return List.of();
        }

        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long requisitionId = testResultDtos.get(0).getRequisitionId();

        // Fetch existing results for this requisition
        Map<Long, TestResult> existingResultsMap = testResultRepository.findByRequisitionId(requisitionId).stream()
                .collect(Collectors.toMap(tr -> tr.getTest().getId(), Function.identity()));

        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"));

        List<TestResult> resultsToSave = testResultDtos.stream().map(dto -> {
            TestResult testResult = existingResultsMap.get(dto.getTestId());
            if (testResult == null) {
                // This is a new result
                testResult = new TestResult();
                testResult.setRequisition(requisition);
                labTestRepository.findById(dto.getTestId()).ifPresent(testResult::setTest);
                labRepository.findById(user.getLabId()).ifPresent(testResult::setLab);
                staffUserRepository.findById(user.getId()).ifPresent(testResult::setEnteredBy);
            }

            // Update values
            testResult.setResultValue(dto.getResultValue());
            testResult.setInterpretation(dto.getInterpretation());

            return testResult;
        }).collect(Collectors.toList());

        List<TestResult> savedTestResults = testResultRepository.saveAll(resultsToSave);

        // Update requisition status to 'Completed'
        requisition.setStatus(SampleStatus.COMPLETED);
        requisition.setCompletionDate(LocalDateTime.now());
        requisitionRepository.save(requisition);

        return savedTestResults.stream()
                .map(testResultMapper::toDto)
                .collect(Collectors.toList());
    }
}
