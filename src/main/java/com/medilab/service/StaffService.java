package com.medilab.service;

import com.medilab.dto.CreateStaffRequest;
import com.medilab.dto.StaffUserDto;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.StaffUserMapper;
import com.medilab.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
    private final StaffUserMapper staffUserMapper;

    @Transactional(readOnly = true)
    public Page<StaffUserDto> getStaffPaged(int page, int limit, String q, String sort, String order, Long labId,
            boolean showDisabled) {
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(direction, sort));

        Specification<StaffUser> spec = (root, query, cb) -> {
            Specification<StaffUser> baseSpec = Specification.where(null);
            if (!showDisabled) {
                baseSpec = baseSpec.and((r, qry, c) -> c.equal(r.get("enabled"), true));
            }
            if (labId != null) {
                baseSpec = baseSpec.and((r, qry, c) -> c.equal(r.get("lab").get("id"), labId));
            }
            return baseSpec.toPredicate(root, query, cb);
        };

        if (StringUtils.hasText(q)) {
            String search = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), search),
                    cb.like(cb.lower(root.get("email")), search),
                    cb.like(cb.lower(root.get("username")), search)));
        }

        return staffUserRepository.findAll(spec, pageable).map(staffUserMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<StaffUser> findAllByLab(Long labId) {
        return staffUserRepository.findByLabIdAndEnabledTrue(labId);
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
                .email(req.getEmail())
                .phone(req.getPhone())
                .role(role)
                .lab(lab)
                .build();

        String tempPassword = StringUtils.hasText(req.getTempPassword())
                ? req.getTempPassword()
                : UUID.randomUUID().toString().substring(0, 8);

        staff.setPassword(passwordEncoder.encode(tempPassword));
        staff.setForcePasswordChange(true);

        log.info("Created staff user: {} with role: {} for lab: {}", username, role,
                lab != null ? lab.getName() : "GLOBAL");
        return staffUserRepository.save(staff);
    }

    @Transactional
    public StaffUser updateStaff(Long staffId, CreateStaffRequest req, Long labId) {
        StaffUser staff = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found"));

        if (labId != null) {
            // If labId is provided, enforce it (Manager case)
            if (staff.getLab() == null || !staff.getLab().getId().equals(labId)) {
                throw new IllegalArgumentException("Unauthorized: Staff does not belong to your lab");
            }
        }

        staff.setName(req.getName());
        staff.setEmail(req.getEmail());
        staff.setPhone(req.getPhone());
        staff.setRole(StaffUser.Role.valueOf(req.getRole()));

        if (StringUtils.hasText(req.getTempPassword())) {
            staff.setPassword(passwordEncoder.encode(req.getTempPassword()));
            staff.setForcePasswordChange(true);
        }

        log.info("Updated staff user: {} (ID: {})", staff.getUsername(), staffId);
        return staffUserRepository.save(staff);
    }

    @Transactional
    public void deleteStaff(Long staffId, Long labId) {
        StaffUser staff = staffUserRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Staff user not found"));

        if (labId != null) {
            if (staff.getLab() == null || !staff.getLab().getId().equals(labId)) {
                throw new IllegalArgumentException("Unauthorized: Staff does not belong to your lab");
            }
        }

        staff.setEnabled(false);
        staff.setUsername(staff.getUsername() + "_del_" + System.currentTimeMillis());
        staffUserRepository.save(staff);

        log.info("Soft-deleted staff user: {} from context lab: {}", staffId, labId);
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
