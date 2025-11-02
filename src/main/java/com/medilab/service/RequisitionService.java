package com.medilab.service;

import com.medilab.dto.RequisitionDto;
import com.medilab.entity.Requisition;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.RequisitionMapper;
import com.medilab.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequisitionService {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private RequisitionMapper requisitionMapper;

    public List<RequisitionDto> getRequisitions() {
        StaffUser user = (StaffUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return requisitionRepository.findByLabId(user.getLab().getId()).stream()
                .map(requisitionMapper::toDto)
                .collect(Collectors.toList());
    }

    public RequisitionDto createRequisition(RequisitionDto requisitionDto) {
        StaffUser user = (StaffUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Requisition requisition = requisitionMapper.toEntity(requisitionDto);

        patientRepository.findById(requisitionDto.getPatientId()).ifPresent(requisition::setPatient);
        labRepository.findById(user.getLab().getId()).ifPresent(requisition::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(requisition::setCreatedBy);

        if (requisitionDto.getTestIds() != null) {
            requisition.setTests(new HashSet<>(labTestRepository.findAllById(requisitionDto.getTestIds())));
        }

        Requisition savedRequisition = requisitionRepository.save(requisition);
        return requisitionMapper.toDto(savedRequisition);
    }
}
