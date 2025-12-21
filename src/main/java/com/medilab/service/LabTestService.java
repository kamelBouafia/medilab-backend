package com.medilab.service;

import com.medilab.dto.LabTestDto;
import com.medilab.entity.Lab;
import com.medilab.entity.LabTest;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.LabTestMapper;
import com.medilab.repository.LabRepository;
import com.medilab.repository.LabTestRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.medilab.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LabTestService {

    private final LabTestRepository labTestRepository;
    private final LabTestMapper labTestMapper;
    private final LabRepository labRepository;
    private final AuditLogService auditLogService;

    public Page<LabTestDto> getLabTests(int page, int limit, String q, String sort, String order) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));

        Specification<LabTest> spec = Specification
                .where((root, query, cb) -> cb.equal(root.get("lab").get("id"), user.getLabId()));

        if (StringUtils.hasText(q)) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"));
        }

        return labTestRepository.findAll(spec, pageable).map(labTestMapper::toDto);
    }

    public LabTestDto addLabTest(LabTestDto labTestDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        LabTest labTest = labTestMapper.toEntity(labTestDto);
        Lab lab = labRepository.findById(user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Lab not found"));
        labTest.setLab(lab);
        LabTest savedLabTest = labTestRepository.save(labTest);
        auditLogService.logAction("LAB_TEST_CREATED",
                "Lab Test '" + savedLabTest.getName() + "' (ID: " + savedLabTest.getId() + ") was created.");
        return labTestMapper.toDto(savedLabTest);
    }

    public LabTestDto updateLabTest(Long testId, LabTestDto labTestDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        LabTest existingLabTest = labTestRepository.findByIdAndLabId(testId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("LabTest not found"));
        existingLabTest.setName(labTestDto.getName());
        existingLabTest.setCategory(labTestDto.getCategory());
        existingLabTest.setPrice(labTestDto.getPrice());
        LabTest updatedLabTest = labTestRepository.save(existingLabTest);
        auditLogService.logAction("LAB_TEST_UPDATED",
                "Lab Test '" + updatedLabTest.getName() + "' (ID: " + updatedLabTest.getId() + ") was updated.");
        return labTestMapper.toDto(updatedLabTest);
    }

    public void deleteLabTest(Long testId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        LabTest existingLabTest = labTestRepository.findByIdAndLabId(testId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("LabTest not found"));
        labTestRepository.deleteById(testId);
        auditLogService.logAction("LAB_TEST_DELETED",
                "Lab Test '" + existingLabTest.getName() + "' (ID: " + existingLabTest.getId() + ") was deleted.");
    }
}
