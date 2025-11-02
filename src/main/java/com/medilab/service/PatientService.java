package com.medilab.service;

import com.medilab.dto.PatientDto;
import com.medilab.entity.Patient;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.PatientMapper;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.repository.LabRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private PatientMapper patientMapper;

    public List<PatientDto> getPatients() {
        StaffUser user = (StaffUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return patientRepository.findByLabId(user.getLab().getId()).stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    public PatientDto createPatient(PatientDto patientDto) {
        StaffUser user = (StaffUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Patient patient = patientMapper.toEntity(patientDto);

        labRepository.findById(user.getLab().getId()).ifPresent(patient::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(patient::setCreatedBy);

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toDto(savedPatient);
    }
}
