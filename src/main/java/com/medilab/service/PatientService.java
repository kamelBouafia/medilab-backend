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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.medilab.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final PatientMapper patientMapper;
    private final AuditLogService auditLogService;
    private final TrialService trialService;

    public Page<PatientDto> getPatients(int page, int limit, String q, String sort, String order) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, limit, Sort.by(direction, sort));

        Specification<Patient> spec = Specification
                .where((root, query, cb) -> cb.equal(root.get("lab").get("id"), user.getLabId()));

        if (StringUtils.hasText(q)) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("contact")), "%" + q.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("username")), "%" + q.toLowerCase() + "%")));
        }

        return patientRepository.findAll(spec, pageable).map(patientMapper::toDto);
    }

    public PatientDto createPatient(PatientDto patientDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Patient patient = patientMapper.toEntity(patientDto);

        labRepository.findById(user.getLabId()).ifPresent(patient::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(patient::setCreatedBy);

        // Check trial status for the lab before creating a patient
        labRepository.findById(user.getLabId()).ifPresent(lab -> trialService.assertTrialActive(lab));

        // Ensure username exists -- use email or phone as default
        if (!StringUtils.hasText(patient.getUsername())) {
            generateUniqueUsername(patient);
        }

        Patient savedPatient = patientRepository.save(patient);
        auditLogService.logAction("PATIENT_CREATED",
                "Patient '" + savedPatient.getName() + "' (ID: " + savedPatient.getId() + ") was created.");
        return patientMapper.toDto(savedPatient);
    }

    private void generateUniqueUsername(Patient patient) {
        String base;
        if (StringUtils.hasText(patient.getEmail()))
            base = patient.getEmail();
        else if (StringUtils.hasText(patient.getPhone()))
            base = patient.getPhone();
        else {
            // fallback to name + timestamp
            base = patient.getName().replaceAll("\\s+", "").toLowerCase();
            if (!StringUtils.hasText(base))
                base = "patient";
        }

        // Try exact match first if it looks like a username (email/phone)
        if (!patientRepository.findByUsername(base).isPresent()) {
            patient.setUsername(base);
            return;
        }

        // If taken, append logic
        String candidate = base;
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            candidate = base + (System.currentTimeMillis() % 10000) + (i > 0 ? "_" + i : "");
            if (!patientRepository.findByUsername(candidate).isPresent()) {
                patient.setUsername(candidate);
                return;
            }
        }
        // Fallback to UUID if all else fails to guarantee uniqueness
        patient.setUsername(base + "_" + java.util.UUID.randomUUID().toString().substring(0, 8));
    }

    public PatientDto getPatientById(Long patientId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Patient patient = patientRepository.findByIdAndLabId(patientId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        return patientMapper.toDto(patient);
    }

    public PatientDto updatePatient(Long patientId, PatientDto patientDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Patient existingPatient = patientRepository.findByIdAndLabId(patientId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patientMapper.updatePatientFromDto(patientDto, existingPatient);

        Patient updatedPatient = patientRepository.save(existingPatient);
        auditLogService.logAction("PATIENT_UPDATED",
                "Patient '" + updatedPatient.getName() + "' (ID: " + updatedPatient.getId() + ") was updated.");
        return patientMapper.toDto(updatedPatient);
    }

    public void deletePatient(Long patientId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Patient existingPatient = patientRepository.findByIdAndLabId(patientId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patientRepository.delete(existingPatient);
        auditLogService.logAction("PATIENT_DELETED",
                "Patient '" + existingPatient.getName() + "' (ID: " + existingPatient.getId() + ") was deleted.");
    }
}
