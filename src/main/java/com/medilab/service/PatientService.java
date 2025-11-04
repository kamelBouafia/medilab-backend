package com.medilab.service;

import com.medilab.dto.PatientDto;
import com.medilab.entity.Patient;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.PatientMapper;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.repository.LabRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final PatientMapper patientMapper;
    private final AuditLogService auditLogService;

    public List<PatientDto> getPatients() {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return patientRepository.findByLabId(user.getLabId()).stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    public PatientDto createPatient(PatientDto patientDto) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Patient patient = patientMapper.toEntity(patientDto);

        labRepository.findById(user.getLabId()).ifPresent(patient::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(patient::setCreatedBy);

        Patient savedPatient = patientRepository.save(patient);
        auditLogService.logAction("PATIENT_CREATED", "Patient '" + savedPatient.getName() + "' (ID: " + savedPatient.getId() + ") was created.");
        return patientMapper.toDto(savedPatient);
    }

    public PatientDto updatePatient(Long patientId, PatientDto patientDto) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Patient existingPatient = patientRepository.findByIdAndLabId(patientId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patientMapper.updatePatientFromDto(patientDto, existingPatient);

        Patient updatedPatient = patientRepository.save(existingPatient);
        auditLogService.logAction("PATIENT_UPDATED", "Patient '" + updatedPatient.getName() + "' (ID: " + updatedPatient.getId() + ") was updated.");
        return patientMapper.toDto(updatedPatient);
    }

    public void deletePatient(Long patientId) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Patient existingPatient = patientRepository.findByIdAndLabId(patientId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patientRepository.delete(existingPatient);
        auditLogService.logAction("PATIENT_DELETED", "Patient '" + existingPatient.getName() + "' (ID: " + existingPatient.getId() + ") was deleted.");
    }
}
