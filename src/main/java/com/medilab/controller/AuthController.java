package com.medilab.controller;

import com.medilab.dto.LoginRequest;
import com.medilab.dto.LoginResponse;
import com.medilab.entity.StaffUser;
import com.medilab.security.JwtUtil;
import com.medilab.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        StaffUser user = (StaffUser) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(user.getUsername());

        return new LoginResponse(jwt);
    }

    @PostMapping("/patient/login")
    public ResponseEntity<?> patientLogin(@RequestBody Map<String, String> credentials) {
        Long labId = Long.parseLong(credentials.get("labId"));
        Long patientId = Long.parseLong(credentials.get("patientId"));
        LocalDate dob = LocalDate.parse(credentials.get("dob"));
        return authService.patientLogin(labId, patientId, dob)
                .map(token -> ResponseEntity.ok(Map.of("jwt", token)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
