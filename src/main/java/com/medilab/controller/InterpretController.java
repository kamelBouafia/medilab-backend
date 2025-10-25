package com.medilab.controller;

import com.medilab.config.TenantPrincipal;
import com.medilab.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class InterpretController {

    private final GeminiService geminiService;

    public InterpretController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/interpret")
    public ResponseEntity<?> interpret(@RequestBody Map<String, String> body, Authentication auth) {
        TenantPrincipal tp = (TenantPrincipal) auth;
        if (tp == null) return ResponseEntity.status(401).build();
        String testName = body.get("testName");
        String resultValue = body.get("resultValue");
        var resp = geminiService.interpret(testName, resultValue);
        return ResponseEntity.ok(resp);
    }
}
