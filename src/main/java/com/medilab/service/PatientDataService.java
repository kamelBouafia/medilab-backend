package com.medilab.service;

import com.medilab.dto.RequisitionDto;
import com.medilab.dto.TestResultDto;
import com.medilab.mapper.RequisitionMapper;
import com.medilab.mapper.TestResultMapper;
import com.medilab.repository.RequisitionRepository;
import com.medilab.repository.TestResultRepository;
import com.medilab.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.medilab.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientDataService {

    private final RequisitionRepository requisitionRepository;
    private final TestResultRepository testResultRepository;
    private final RequisitionMapper requisitionMapper;
    private final TestResultMapper testResultMapper;

    @Transactional
    public List<RequisitionDto> getPatientRequisitions() {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        return requisitionRepository.findByPatientIdAndLabIdWithTests(user.getId(), user.getLabId()).stream()
                .map(requisitionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TestResultDto> getPatientTestResults(Long reqId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        // Add logic to ensure the requisition belongs to the authenticated patient
        return testResultRepository.findByRequisitionIdAndLabId(reqId, user.getLabId()).stream()
                .map(testResultMapper::toDto)
                .collect(Collectors.toList());
    }
}
