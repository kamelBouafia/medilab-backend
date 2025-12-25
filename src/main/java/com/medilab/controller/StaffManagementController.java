package com.medilab.controller;

import com.medilab.dto.CreateStaffRequest;
import com.medilab.dto.StaffUserDto;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.StaffUserMapper;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.SecurityUtils;
import com.medilab.service.LabService;
import com.medilab.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffManagementController {

    private final StaffService staffService;
    private final LabService labService;
    private final StaffUserMapper staffUserMapper;

    @PostMapping
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<StaffUserDto> addStaff(@Valid @RequestBody CreateStaffRequest req) {
        AuthenticatedUser auth = SecurityUtils.getAuthenticatedUser();
        Lab lab = labService.findById(auth.getLabId()).orElseThrow(() -> new IllegalStateException("Lab not found"));
        StaffUser created = staffService.createStaff(lab, req);
        return new ResponseEntity<>(staffUserMapper.toDto(created), HttpStatus.CREATED);
    }
}
