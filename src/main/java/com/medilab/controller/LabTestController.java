package com.medilab.controller;

import com.medilab.entity.LabTest;
import com.medilab.service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lab-tests")
public class LabTestController {

    private final LabTestService labTestService;

    @Autowired
    public LabTestController(LabTestService labTestService) {
        this.labTestService = labTestService;
    }

    @GetMapping
    public ResponseEntity<List<LabTest>> getLabTests() {
        return ResponseEntity.ok(labTestService.getAllLabTests());
    }
}
