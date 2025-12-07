package com.medilab.service;

import com.medilab.dto.CreateStaffRequest;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {
    private final StaffUserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public List<StaffUser> findAllByLab(Long labId){ return repo.findByLabId(labId); }
    public java.util.Optional<StaffUser> findById(Long id){ return repo.findById(id); }

    public StaffUser createStaff(Lab lab, CreateStaffRequest req) {
        StaffUser.Role role = StaffUser.Role.valueOf(req.getRole());

        // ensure unique username
        String base = req.getUsername();
        if (base == null || base.isBlank()) {
            base = req.getName().replaceAll("\\s+", "").toLowerCase();
            if (base.isBlank()) base = "staff" + System.currentTimeMillis();
        }
        String candidate = base;
        int suffix = 1;
        while (repo.findByUsername(candidate).isPresent()) {
            candidate = base + suffix;
            suffix++;
        }

        StaffUser staff = StaffUser.builder()
                .name(req.getName())
                .username(candidate)
                .email(null)
                .phone(null)
                .role(role)
                .lab(lab)
                .forcePasswordChange(false)
                .build();

        if (req.getTempPassword() != null && !req.getTempPassword().isBlank()) {
            String encoded = passwordEncoder.encode(req.getTempPassword());
            staff.setPassword(encoded);
            staff.setForcePasswordChange(true);
        } else {
            // generate a random temporary password
            String temp = java.util.UUID.randomUUID().toString().substring(0, 8);
            staff.setPassword(passwordEncoder.encode(temp));
            staff.setForcePasswordChange(true);
        }

        return repo.save(staff);
    }
}
