package com.medilab.service;

import com.medilab.entity.Patient;
import com.medilab.entity.StaffUser;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private JwtUtil jwtUtil;

//    public Optional<String> staffLogin(Long labId, Long userId) {
//        Optional<StaffUser> user = staffUserRepository.findById(userId);
//        if (user.isPresent() && user.get().getLab().getId().equals(labId)) {
//            return Optional.of(jwtUtil.generateToken(user.get().getUsername()));
//        }
//        return Optional.empty();
//    }

    public Optional<String> patientLogin(Long labId, Long patientId, LocalDate dob) {
        Optional<Patient> patient = patientRepository.findByLabIdAndIdAndDob(labId, patientId, dob);
        return patient.map(value -> jwtUtil.generateToken(value.getName()));
    }
}
