package com.medilab.service;

import com.medilab.dto.RequisitionDto;
import com.medilab.dto.TestResultDto;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.RequisitionMapper;
import com.medilab.mapper.TestResultMapper;
import com.medilab.repository.RequisitionRepository;
import com.medilab.repository.TestResultRepository;
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
        StaffUser user = (StaffUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return requisitionRepository.findByPatientIdAndLabId(user.getId(), user.getLab().getId()).stream()
                .map(requisitionMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<TestResultDto> getPatientTestResults(Long reqId) {
        StaffUser user = (StaffUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Add logic to ensure the requisition belongs to the authenticated patient
        return testResultRepository.findByRequisitionIdAndLabId(reqId, user.getLab().getId()).stream()
                .map(testResultMapper::toDto)
                .collect(Collectors.toList());
    }
}
