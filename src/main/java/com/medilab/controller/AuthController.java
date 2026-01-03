package com.medilab.controller;

import com.medilab.dto.ChangePasswordRequest;
import com.medilab.dto.LabRegistrationRequest;
import com.medilab.dto.LoginRequest;
import com.medilab.dto.LoginResponse;
import com.medilab.dto.PatientLoginRequest;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.JwtUtil;
import com.medilab.service.AuditLogService;
import com.medilab.service.LabService;
import com.medilab.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LabService labService;
    private final StaffService staffService;
    private final StaffUserRepository staffUserRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        // Ensure lab trial is active before issuing token
        boolean isSystemAdmin = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("SYSTEM_ADMIN"));

        if (!isSystemAdmin) {
            // labService.findById(user.getLabId()).ifPresent(trialService::assertTrialActive);
            // Allow login even if expired, read-only mode will be enforced by interceptor
        }

        String jwt = jwtUtil.generateToken(user);

        auditLogService.logAction(user, "LOGIN", "User logged in: " + user.getUsername());

        return new LoginResponse(jwt);
    }

    @PostMapping("/patient/login")
    public LoginResponse patientLogin(@Valid @RequestBody PatientLoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getDob().toString()));

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        // Ensure lab trial is active for patient's lab as well
        // labService.findById(user.getLabId()).ifPresent(trialService::assertTrialActive);
        // Patients should always be able to enter the system

        String jwt = jwtUtil.generateToken(user);

        auditLogService.logAction(user, "PATIENT_LOGIN", "Patient logged in: " + user.getUsername());

        return new LoginResponse(jwt);
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody LabRegistrationRequest req) {
        // Create Lab
        LocalDateTime now = LocalDateTime.now();
        Lab lab = Lab.builder()
                .name(req.getLabName())
                .location(req.getLocation())
                .contactEmail(req.getContactEmail())
                .licenseNumber(req.getLicenseNumber())
                .trialStart(now)
                .trialEnd(now.plusDays(30))
                .build();
        Lab savedLab = labService.createLab(lab);

        // Create Manager staff via StaffService (this will encode password and set
        // force change)
        com.medilab.dto.CreateStaffRequest createReq = new com.medilab.dto.CreateStaffRequest();
        createReq.setName(req.getAdminName());
        createReq.setUsername(req.getAdminUsername());
        createReq.setRole(StaffUser.Role.Manager.name());
        createReq.setTempPassword(req.getAdminPassword());

        StaffUser savedManager = staffService.createStaff(savedLab, createReq);

        // For the initial Lab owner, don't force password change
        savedManager.setForcePasswordChange(false);
        staffUserRepository.save(savedManager);

        // Authenticate new user and return token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getAdminUsername(), req.getAdminPassword()));

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(user);

        auditLogService.logAction(user, "REGISTER_LAB", "New lab registered: " + req.getLabName());

        return new LoginResponse(jwt);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        // Authenticate first
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getOldPassword()));
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        if ("staff".equals(user.getUserType())) {
            staffUserRepository.findById(user.getId()).ifPresent(staff -> {
                staff.setPassword(passwordEncoder.encode(req.getNewPassword()));
                staff.setForcePasswordChange(false);
                staffUserRepository.save(staff);
            });
        } else if ("patient".equals(user.getUserType())) {
            patientRepository.findById(user.getId()).ifPresent(patient -> {
                patient.setDob(java.time.LocalDate.parse(req.getNewPassword()));
                // Patient passwords are DOB; to change we update the dob field. Not ideal but
                // matches current auth design.
                patientRepository.save(patient);
            });
        }

        auditLogService.logAction(user, "CHANGE_PASSWORD", "Password changed for user: " + user.getUsername());

        return ResponseEntity.ok().build();
    }
}
