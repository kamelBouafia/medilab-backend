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

    @GetMapping
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<org.springframework.data.domain.Page<StaffUserDto>> getStaff(
            @RequestParam(defaultValue = "1") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "name") String _sort,
            @RequestParam(defaultValue = "asc") String _order) {
        return ResponseEntity.ok(staffService.getStaffPaged(_page, _limit, q, _sort, _order));
    }

    @PostMapping
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<StaffUserDto> addStaff(@Valid @RequestBody CreateStaffRequest req) {
        AuthenticatedUser auth = SecurityUtils.getAuthenticatedUser();
        Lab lab = labService.findById(auth.getLabId()).orElseThrow(() -> new IllegalStateException("Lab not found"));
        StaffUser created = staffService.createStaff(lab, req);
        return new ResponseEntity<>(staffUserMapper.toDto(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<StaffUserDto> updateStaff(@PathVariable Long id, @Valid @RequestBody CreateStaffRequest req) {
        AuthenticatedUser auth = SecurityUtils.getAuthenticatedUser();
        StaffUser updated = staffService.updateStaff(auth.getLabId(), id, req);
        return ResponseEntity.ok(staffUserMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        AuthenticatedUser auth = SecurityUtils.getAuthenticatedUser();
        staffService.deleteStaff(auth.getLabId(), id);
        return ResponseEntity.noContent().build();
    }
}
