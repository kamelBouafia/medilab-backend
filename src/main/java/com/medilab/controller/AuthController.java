package com.medilab.controller;

import com.medilab.config.JwtUtil;
import com.medilab.entity.Patient;
import com.medilab.entity.StaffUser;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StaffUserRepository staffRepo;
    private final PatientRepository patientRepo;
    private final JwtUtil jwtUtil;

    public AuthController(StaffUserRepository staffRepo, PatientRepository patientRepo, JwtUtil jwtUtil) {
        this.staffRepo = staffRepo;
        this.patientRepo = patientRepo;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/staff/login")
    public ResponseEntity<?> staffLogin(@RequestBody Map<String, String> body) {
        String labId = body.get("labId");
        String userId = body.get("userId");
        var userOpt = staffRepo.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().getLabId().equals(labId)) return ResponseEntity.status(401).build();
        StaffUser user = userOpt.get();
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("name", user.getName());
        claims.put("role", user.getRole().name());
        claims.put("labId", user.getLabId());
        String token = jwtUtil.generateToken(claims, 24 * 3600 * 1000L);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/patient/login")
    public ResponseEntity<?> patientLogin(@RequestBody Map<String, String> body) {
        String labId = body.get("labId");
        String patientId = body.get("patientId");
        var dob = body.get("dob");
        var pOpt = patientRepo.findById(patientId);
        if (pOpt.isEmpty() || !pOpt.get().getLabId().equals(labId)) return ResponseEntity.status(401).build();
        Patient p = pOpt.get();
        if (!p.getDob().toString().equals(dob)) return ResponseEntity.status(401).build();
        Map<String, Object> claims = new HashMap<>();
        claims.put("patientId", p.getId());
        claims.put("name", p.getName());
        claims.put("labId", p.getLabId());
        claims.put("role", "patient");
        String token = jwtUtil.generateToken(claims, 24 * 3600 * 1000L);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
