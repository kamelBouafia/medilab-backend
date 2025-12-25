package com.medilab.controller;

import com.medilab.dto.StaffUserDto;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.StaffUserMapper;
import com.medilab.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;
    private final StaffUserMapper staffUserMapper;

    @GetMapping("/staff-init/{labId}")
    public ResponseEntity<List<StaffUserDto>> staffInit(@PathVariable Long labId) {
        List<StaffUser> staffList = staffService.findAllByLab(labId);
        return ResponseEntity.ok(staffUserMapper.toDtoList(staffList));
    }
}
