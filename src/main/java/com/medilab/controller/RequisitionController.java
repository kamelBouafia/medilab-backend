package com.medilab.controller;

import com.medilab.dto.RequisitionDto;
import com.medilab.service.RequisitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requisitions")
@PreAuthorize("hasRole('Staff')")
public class RequisitionController {

    @Autowired
    private RequisitionService requisitionService;

    @GetMapping
    public ResponseEntity<List<RequisitionDto>> getRequisitions() {
        return ResponseEntity.ok(requisitionService.getRequisitions());
    }

    @PostMapping
    public ResponseEntity<RequisitionDto> createRequisition(@RequestBody RequisitionDto requisitionDto) {
        RequisitionDto createdRequisition = requisitionService.createRequisition(requisitionDto);
        return new ResponseEntity<>(createdRequisition, HttpStatus.CREATED);
    }
}
