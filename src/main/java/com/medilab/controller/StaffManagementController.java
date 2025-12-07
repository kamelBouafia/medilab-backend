package com.medilab.controller;

import com.medilab.dto.CreateStaffRequest;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.security.AuthenticatedUser;
import com.medilab.service.LabService;
import com.medilab.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffManagementController {

    private final StaffService staffService;
    private final LabService labService;

    @PostMapping
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<StaffUser> addStaff(@Valid @RequestBody CreateStaffRequest req) {
        AuthenticatedUser auth = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long labId = auth.getLabId();
        Lab lab = labService.findById(labId).orElseThrow(() -> new IllegalStateException("Lab not found"));
        StaffUser created = staffService.createStaff(lab, req);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}

