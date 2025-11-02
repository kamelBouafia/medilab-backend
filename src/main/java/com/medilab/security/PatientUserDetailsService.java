package com.medilab.security;

import com.medilab.entity.Patient;
import com.medilab.repository.PatientRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("patientUserDetailsService")
public class PatientUserDetailsService implements UserDetailsService {

    private final PatientRepository patientRepository;

    public PatientUserDetailsService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] parts = username.split("-");
        if (parts.length != 2) {
            throw new UsernameNotFoundException("Invalid username format");
        }

        Long labId = Long.parseLong(parts[0]);
        Long patientId = Long.parseLong(parts[1]);

        Patient patient = patientRepository.findByLabIdAndId(labId, patientId)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found with username: " + username));

        return new AuthenticatedUser(
                patient.getId(),
                patient.getLab().getId(),
                patient.getLab().getId() + "-" + patient.getId(),
                patient.getDob().toString(),
                List.of(new SimpleGrantedAuthority("ROLE_PATIENT")),
                "patient"
        );
    }
}
