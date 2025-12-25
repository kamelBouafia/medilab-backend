package com.medilab.service;

import com.medilab.dto.CreateStaffRequest;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<StaffUser> findAllByLab(Long labId) {
        return staffUserRepository.findByLabId(labId);
    }

    @Transactional(readOnly = true)
    public Optional<StaffUser> findById(Long id) {
        return staffUserRepository.findById(id);
    }

    @Transactional
    public StaffUser createStaff(Lab lab, CreateStaffRequest req) {
        StaffUser.Role role = StaffUser.Role.valueOf(req.getRole());
        String username = generateUniqueUsername(req);

        StaffUser staff = StaffUser.builder()
                .name(req.getName())
                .username(username)
                .role(role)
                .lab(lab)
                .build();

        String tempPassword = StringUtils.hasText(req.getTempPassword())
                ? req.getTempPassword()
                : UUID.randomUUID().toString().substring(0, 8);

        staff.setPassword(passwordEncoder.encode(tempPassword));
        staff.setForcePasswordChange(true);

        log.info("Created staff user: {} with role: {} for lab: {}", username, role, lab.getName());
        return staffUserRepository.save(staff);
    }

    private String generateUniqueUsername(CreateStaffRequest req) {
        String base = StringUtils.hasText(req.getUsername())
                ? req.getUsername()
                : req.getName().replaceAll("\\s+", "").toLowerCase();

        if (!StringUtils.hasText(base))
            base = "staff";

        String candidate = base;
        int suffix = 1;
        while (staffUserRepository.findByUsername(candidate).isPresent()) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}
