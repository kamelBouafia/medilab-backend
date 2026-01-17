package com.medilab.controller;

import com.medilab.dto.CreateStaffRequest;
import com.medilab.dto.StaffUserDto;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.exception.ResourceNotFoundException;
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
    @PreAuthorize("hasAnyRole('Manager', 'SYSTEM_ADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<StaffUserDto>> getStaff(
            @RequestParam(defaultValue = "1") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "name") String _sort,
            @RequestParam(defaultValue = "asc") String _order,
            @RequestParam(required = false) Long labId,
            @RequestParam(defaultValue = "false") boolean showDeactivated) {

        AuthenticatedUser auth = SecurityUtils.getAuthenticatedUser();
        boolean isSysAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SYSTEM_ADMIN"));

        Long finalLabId = isSysAdmin ? labId : auth.getLabId();
        boolean finalShowDeactivated = isSysAdmin && showDeactivated;

        return ResponseEntity
                .ok(staffService.getStaffPaged(_page, _limit, q, _sort, _order, finalLabId, finalShowDeactivated));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Manager', 'SYSTEM_ADMIN')")
    public ResponseEntity<StaffUserDto> addStaff(@Valid @RequestBody CreateStaffRequest req) {
        AuthenticatedUser auth = SecurityUtils.getAuthenticatedUser();
        boolean isSysAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SYSTEM_ADMIN"));

        if (!isSysAdmin && auth.getParentLabId() != null) {
            throw new org.springframework.security.access.AccessDeniedException("Branch labs cannot manage staff.");
        }

        Long requestedLabId = req.getLabId();
        Long finalLabId;
        if (isSysAdmin) {
            finalLabId = requestedLabId;
        } else {
            // Manager
            if (requestedLabId == null || requestedLabId.equals(auth.getLabId())) {
                finalLabId = auth.getLabId();
            } else {
                // Check if requestedLabId is a branch of auth.getLabId()
                Lab requestedLab = labService.findById(requestedLabId)
                        .orElseThrow(() -> new ResourceNotFoundException("Lab not found"));
                if (requestedLab.getParentLab() != null
                        && requestedLab.getParentLab().getId().equals(auth.getLabId())) {
                    finalLabId = requestedLabId;
                } else {
                    throw new org.springframework.security.access.AccessDeniedException(
                            "You can only add staff to your own lab or its branches.");
                }
            }
        }

        Lab lab = null;
        if (finalLabId != null) {
            lab = labService.findById(finalLabId).orElseThrow(() -> new IllegalStateException("Lab not found"));
        }

        StaffUser created = staffService.createStaff(lab, req);
        return new ResponseEntity<>(staffUserMapper.toDto(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager', 'SYSTEM_ADMIN')")
    public ResponseEntity<StaffUserDto> updateStaff(@PathVariable Long id, @Valid @RequestBody CreateStaffRequest req) {
        AuthenticatedUser auth = SecurityUtils.getAuthenticatedUser();
        boolean isSysAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SYSTEM_ADMIN"));

        Long contextLabId = isSysAdmin ? null : auth.getLabId();
        StaffUser updated = staffService.updateStaff(id, req, contextLabId);
        return ResponseEntity.ok(staffUserMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        AuthenticatedUser auth = SecurityUtils.getAuthenticatedUser();
        boolean isSysAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SYSTEM_ADMIN"));

        Long contextLabId = isSysAdmin ? null : auth.getLabId();
        staffService.deleteStaff(id, contextLabId);
        return ResponseEntity.noContent().build();
    }
}
