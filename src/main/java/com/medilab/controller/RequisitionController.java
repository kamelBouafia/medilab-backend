package com.medilab.controller;

import com.medilab.entity.RequisitionTest;
import com.medilab.service.TestRequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requisitions")
public class RequisitionController {

    private final TestRequisitionService testRequisitionService;

    @Autowired
    public RequisitionController(TestRequisitionService testRequisitionService) {
        this.testRequisitionService = testRequisitionService;
    }

    @GetMapping
    public ResponseEntity<List<RequisitionTest>> getRequisitions() {
        return ResponseEntity.ok(testRequisitionService.getAllRequisitions());
    }

    @PostMapping
    public ResponseEntity<RequisitionTest> addRequisition(@RequestBody RequisitionTest testRequisition) {
        return ResponseEntity.ok(testRequisitionService.addRequisition(testRequisition));
    }
}
