package com.medilab.controller;

import com.medilab.service.InterpretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/interpret")
@PreAuthorize("hasRole('Staff')")
public class InterpretController {

    @Autowired
    private InterpretService interpretService;

    @PostMapping
    public ResponseEntity<?> getInterpretation(@RequestBody Map<String, String> body) {
        String testName = body.get("testName");
        String resultValue = body.get("resultValue");
        String interpretation = interpretService.getInterpretation(testName, resultValue);
        return ResponseEntity.ok(Map.of("interpretation", interpretation));
    }
}
