//package com.medilab.controller;
//
//import com.medilab.dto.StaffUserDto;
//import com.medilab.mapper.StaffUserMapper;
//import com.medilab.repository.StaffUserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/staff-init")
//public class StaffInitController {
//
//    @Autowired
//    private StaffUserRepository staffUserRepository;
//
//    @Autowired
//    private StaffUserMapper staffUserMapper;
//
//    @GetMapping("/{labId}")
//    public ResponseEntity<List<StaffUserDto>> getStaffForLogin(@PathVariable Long labId) {
//        return ResponseEntity.ok(staffUserRepository.findByLabId(labId).stream()
//                .map(staffUserMapper::toDto)
//                .collect(Collectors.toList()));
//    }
//}
