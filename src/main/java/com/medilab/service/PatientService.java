package com.medilab.service;

import com.medilab.dto.PatientDto;
import com.medilab.entity.Patient;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.PatientMapper;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.repository.LabRepository;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final PatientMapper patientMapper;
    private final AuditLogService auditLogService;
    private final TrialService trialService;

    @Transactional(readOnly = true)
    public Page<PatientDto> getPatients(int page, int limit, String q, String sort, String order) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(direction, sort));

        Specification<Patient> spec = (root, query, cb) -> cb.equal(root.get("lab").get("id"), user.getLabId());

        if (StringUtils.hasText(q)) {
            String search = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), search),
                    cb.like(cb.lower(root.get("phone")), search), // Changed from contact to phone
                    cb.like(cb.lower(root.get("username")), search)));
        }

        return patientRepository.findAll(spec, pageable).map(patientMapper::toDto);
    }

    @Transactional
    public PatientDto createPatient(PatientDto patientDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        labRepository.findById(user.getLabId()).ifPresent(trialService::assertTrialActive);

        Patient patient = patientMapper.toEntity(patientDto);
        patient.setLab(labRepository.getReferenceById(user.getLabId()));
        patient.setCreatedBy(staffUserRepository.getReferenceById(user.getId()));

        if (!StringUtils.hasText(patient.getUsername())) {
            patient.setUsername(generateUniqueUsername(patient));
        }

        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created (ID: {})", savedPatient.getId());
        auditLogService.logAction("PATIENT_CREATED",
                "Patient ID: " + savedPatient.getId() + " was created.");
        return patientMapper.toDto(savedPatient);
    }

    private String generateUniqueUsername(Patient patient) {
        String base = StringUtils.hasText(patient.getEmail()) ? patient.getEmail()
                : (StringUtils.hasText(patient.getPhone()) ? patient.getPhone()
                        : patient.getName().replaceAll("\\s+", "").toLowerCase());

        if (!StringUtils.hasText(base))
            base = "patient";

        if (patientRepository.findByUsername(base).isEmpty()) {
            return base;
        }

        // Try with append
        for (int i = 1; i <= 5; i++) {
            String candidate = base + "_" + (System.currentTimeMillis() % 1000);
            if (patientRepository.findByUsername(candidate).isEmpty()) {
                return candidate;
            }
        }

        return base + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Transactional(readOnly = true)
    public PatientDto getPatientById(Long patientId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        return patientRepository.findByIdAndLabId(patientId, user.getLabId())
                .map(patientMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
    }

    @Transactional
    public PatientDto updatePatient(Long patientId, PatientDto patientDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Patient existingPatient = patientRepository.findByIdAndLabId(patientId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        patientMapper.updatePatientFromDto(patientDto, existingPatient);

        Patient updatedPatient = patientRepository.save(existingPatient);
        log.info("Patient updated (ID: {})", updatedPatient.getId());
        auditLogService.logAction("PATIENT_UPDATED",
                "Patient ID: " + updatedPatient.getId() + " was updated.");
        return patientMapper.toDto(updatedPatient);
    }

    @Transactional
    public void deletePatient(Long patientId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Patient patient = patientRepository.findByIdAndLabId(patientId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        // GDPR Right to be Forgotten: Anonymize sensitive PII while keeping clinical
        // record structures
        patient.setName("ANONYMIZED_" + patient.getId());
        patient.setEmail("anonymized_" + patient.getId() + "@example.com");
        patient.setPhone(null);
        patient.setContact(null);
        patient.setAddress(null);
        patient.setAllergies(null);
        patient.setUsername("anonymized_" + patient.getId() + "_" + System.currentTimeMillis() % 1000);

        patientRepository.save(patient);

        log.info("Patient anonymized (ID: {})", patient.getId());
        auditLogService.logAction("PATIENT_ANONYMIZED",
                "Patient ID: " + patient.getId() + " was anonymized (Right to be Forgotten).");
    }
}
