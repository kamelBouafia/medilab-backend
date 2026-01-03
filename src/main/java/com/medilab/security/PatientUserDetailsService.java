package com.medilab.security;

import com.medilab.entity.Patient;
import com.medilab.repository.PatientRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class PatientUserDetailsService implements UserDetailsService {

    private final PatientRepository patientRepository;

    public PatientUserDetailsService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Patient patient = patientRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found with username: " + username));

        return new AuthenticatedUser(
                patient.getId(),
                patient.getLab().getId(),
                patient.getUsername(),
                patient.getDob().toString(),
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT")),
                "patient",
                false,
                patient.isGdprAccepted(),
                true,
                patient.getLab().getTrialEnd());
    }
}
