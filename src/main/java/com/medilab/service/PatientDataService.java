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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public List<TestResultDto> getPatientTestResults(Long reqId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        return testResultRepository.findByRequisitionIdAndLabId(reqId, user.getLabId()).stream()
                .map(testResultMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> exportFullPatientData(Long patientId, Long labId) {
        Map<String, Object> export = new HashMap<>();

        // This method can be called by patient themselves or by staff
        // If called by patient, patientId must match authenticated user id

        List<RequisitionDto> requisitions = requisitionRepository.findByPatientIdAndLabIdWithTests(patientId, labId)
                .stream()
                .map(requisitionMapper::toDto)
                .collect(Collectors.toList());

        List<TestResultDto> allResults = new ArrayList<>();
        for (RequisitionDto req : requisitions) {
            allResults.addAll(testResultRepository.findByRequisitionIdAndLabId(req.getId(), labId).stream()
                    .map(testResultMapper::toDto)
                    .collect(Collectors.toList()));
        }

        export.put("exportedAt", java.time.OffsetDateTime.now());
        export.put("requisitions", requisitions);
        export.put("testResults", allResults);

        return export;
    }
}
