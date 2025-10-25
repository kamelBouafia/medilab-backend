package com.medilab.service;

import com.medilab.dto.RequisitionDto;
import com.medilab.dto.TestResultDto;
import com.medilab.mapper.RequisitionMapper;
import com.medilab.mapper.TestResultMapper;
import com.medilab.repository.RequisitionRepository;
import com.medilab.repository.TestResultRepository;
import com.medilab.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientDataService {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private RequisitionMapper requisitionMapper;

    @Autowired
    private TestResultMapper testResultMapper;

    public List<RequisitionDto> getPatientRequisitions() {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return requisitionRepository.findByPatientIdAndLabId(Long.parseLong(user.getUsername()), user.getLabId()).stream()
                .map(requisitionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TestResultDto> getPatientTestResults(Long reqId) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Add logic to ensure the requisition belongs to the authenticated patient
        return testResultRepository.findByRequisitionIdAndLabId(reqId, user.getLabId()).stream()
                .map(testResultMapper::toDto)
                .collect(Collectors.toList());
    }
}
