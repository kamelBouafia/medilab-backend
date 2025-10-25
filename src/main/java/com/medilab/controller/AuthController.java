package com.medilab.controller;

import com.medilab.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/staff/login")
    public ResponseEntity<?> staffLogin(@RequestBody Map<String, String> credentials) {
        Long labId = Long.parseLong(credentials.get("labId"));
        Long userId = Long.parseLong(credentials.get("userId"));
        return authService.staffLogin(labId, userId)
                .map(token -> ResponseEntity.ok(Map.of("token", token)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/patient/login")
    public ResponseEntity<?> patientLogin(@RequestBody Map<String, String> credentials) {
        Long labId = Long.parseLong(credentials.get("labId"));
        Long patientId = Long.parseLong(credentials.get("patientId"));
        LocalDate dob = LocalDate.parse(credentials.get("dob"));
        return authService.patientLogin(labId, patientId, dob)
                .map(token -> ResponseEntity.ok(Map.of("token", token)))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
