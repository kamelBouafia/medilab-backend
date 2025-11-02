package com.medilab.controller;

import com.medilab.dto.LoginRequest;
import com.medilab.dto.LoginResponse;
import com.medilab.dto.PatientLoginRequest;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(user);

        return new LoginResponse(jwt);
    }

    @PostMapping("/patient/login")
    public LoginResponse patientLogin(@RequestBody PatientLoginRequest loginRequest) {
        String username = loginRequest.getLabId() + "-" + loginRequest.getPatientId();
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getDob().toString()));

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(user);

        return new LoginResponse(jwt);
    }
}
