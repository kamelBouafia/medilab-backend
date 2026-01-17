package com.medilab.service;

import com.medilab.dto.LabTestDto;
import com.medilab.entity.GlobalTestCatalog;
import com.medilab.entity.Lab;
import com.medilab.entity.LabTest;
import com.medilab.enums.TestUnit;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.LabTestMapper;
import com.medilab.repository.GlobalTestCatalogRepository;
import com.medilab.repository.LabRepository;
import com.medilab.repository.LabTestRepository;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabTestService {

        private final LabTestRepository labTestRepository;
        private final LabTestMapper labTestMapper;
        private final LabRepository labRepository;
        private final AuditLogService auditLogService;
        private final GlobalTestCatalogRepository globalTestCatalogRepository;

        @Transactional(readOnly = true)
        public Page<LabTestDto> getLabTests(int page, int limit, String q, String sort, String order) {
                AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
                Sort.Direction direction = Sort.Direction.fromString(order);
                Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(direction, sort));

                Specification<LabTest> spec = (root, query, cb) -> {
                        if (user.getParentLabId() == null) {
                                // Main Lab: see own and branches
                                return cb.or(
                                                cb.equal(root.get("lab").get("id"), user.getLabId()),
                                                cb.equal(root.get("lab").get("parentLab").get("id"), user.getLabId()));
                        } else {
                                // Branch Lab: see own and parent's
                                return cb.or(
                                                cb.equal(root.get("lab").get("id"), user.getLabId()),
                                                cb.equal(root.get("lab").get("id"), user.getParentLabId()));
                        }
                };

                if (StringUtils.hasText(q)) {
                        spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")),
                                        "%" + q.toLowerCase() + "%"));
                }

                return labTestRepository.findAll(spec, pageable).map(labTestMapper::toDto);
        }

        @Transactional
        public LabTestDto addLabTest(LabTestDto labTestDto) {
                AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
                if (user.getParentLabId() != null) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Branch labs cannot manage lab tests.");
                }
                LabTest labTest = labTestMapper.toEntity(labTestDto);
                Lab lab = labRepository.findById(user.getLabId())
                                .orElseThrow(() -> new ResourceNotFoundException("Lab not found"));
                labTest.setLab(lab);

                if (labTest.getCode() == null) {
                        labTest.setCode("CUSTOM-" + System.currentTimeMillis());
                }

                LabTest savedLabTest = labTestRepository.save(labTest);
                log.info("Lab Test created: {} (ID: {})", savedLabTest.getName(), savedLabTest.getId());
                auditLogService.logAction("LAB_TEST_CREATED",
                                "Lab Test '" + savedLabTest.getName() + "' (ID: " + savedLabTest.getId()
                                                + ") was created.");
                return labTestMapper.toDto(savedLabTest);
        }

        @Transactional
        public LabTestDto updateLabTest(Long testId, LabTestDto labTestDto) {
                AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
                LabTest existingLabTest = labTestRepository.findByIdAndHierarchicalLabId(testId, user.getLabId())
                                .orElseThrow(() -> new ResourceNotFoundException("LabTest not found"));

                labTestMapper.updateEntityFromDto(labTestDto, existingLabTest);

                LabTest updatedLabTest = labTestRepository.save(existingLabTest);
                log.info("Lab Test updated: {} (ID: {})", updatedLabTest.getName(), updatedLabTest.getId());
                auditLogService.logAction("LAB_TEST_UPDATED",
                                "Lab Test '" + updatedLabTest.getName() + "' (ID: " + updatedLabTest.getId()
                                                + ") was updated.");
                return labTestMapper.toDto(updatedLabTest);
        }

        @Transactional
        public void deleteLabTest(Long testId) {
                AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
                LabTest existingLabTest = labTestRepository.findByIdAndHierarchicalLabId(testId, user.getLabId())
                                .orElseThrow(() -> new ResourceNotFoundException("LabTest not found"));

                labTestRepository.delete(existingLabTest);
                log.info("Lab Test deleted: {} (ID: {})", existingLabTest.getName(), existingLabTest.getId());
                auditLogService.logAction("LAB_TEST_DELETED",
                                "Lab Test '" + existingLabTest.getName() + "' (ID: " + existingLabTest.getId()
                                                + ") was deleted.");
        }

        @Transactional
        public java.util.List<LabTestDto> importTestsFromGlobal(
                        java.util.List<com.medilab.dto.BulkImportDto.ImportItem> items) {
                java.util.List<LabTestDto> importedTests = new ArrayList<>();
                AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

                for (com.medilab.dto.BulkImportDto.ImportItem item : items) {
                        try {
                                if (labTestRepository
                                                .findByGlobalTestIdAndLabId(item.getGlobalTestId(), user.getLabId())
                                                .isPresent()) {
                                        continue;
                                }
                                importedTests.add(importTestFromGlobal(item.getGlobalTestId(), item.getPrice()));
                        } catch (Exception e) {
                                log.error("Failed to import test " + item.getGlobalTestId(), e);
                        }
                }
                return importedTests;
        }

        @Transactional
        public LabTestDto importTestFromGlobal(Long globalTestId, java.math.BigDecimal price) {
                AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
                if (user.getParentLabId() != null) {
                        throw new org.springframework.security.access.AccessDeniedException(
                                        "Branch labs cannot manage lab tests.");
                }
                GlobalTestCatalog globalTest = globalTestCatalogRepository.findById(globalTestId)
                                .orElseThrow(() -> new ResourceNotFoundException("Global Test not found"));

                if (labTestRepository.findByGlobalTestIdAndLabId(globalTestId, user.getLabId()).isPresent()) {
                        throw new IllegalArgumentException("Test already imported");
                }

                Lab lab = labRepository.findById(user.getLabId())
                                .orElseThrow(() -> new ResourceNotFoundException("Lab not found"));

                LabTest labTest = new LabTest();
                labTest.setGlobalTest(globalTest);
                labTest.setLab(lab);
                labTest.setCode(globalTest.getCode());
                labTest.setCategory(globalTest.getCategory());
                labTest.setUnit(resolveUnit(globalTest.getDefaultUnit()));
                labTest.setDescription(globalTest.getDescription());
                labTest.setPrice(price);

                String lang = lab.getDefaultLanguage() != null ? lab.getDefaultLanguage() : "en";
                String name = globalTest.getNames().getOrDefault(lang,
                                globalTest.getNames().getOrDefault("en", globalTest.getCode()));
                labTest.setName(name);
                labTest.setReferenceRanges(new ArrayList<>());

                LabTest savedLabTest = labTestRepository.save(labTest);
                log.info("Lab Test imported from global catalog: {} (ID: {})", savedLabTest.getName(),
                                savedLabTest.getId());
                auditLogService.logAction("LAB_TEST_IMPORTED",
                                "Lab Test '" + savedLabTest.getName() + "' (ID: " + savedLabTest.getId()
                                                + ") was imported.");

                return labTestMapper.toDto(savedLabTest);
        }

        private TestUnit resolveUnit(String unitStr) {
                if (!StringUtils.hasText(unitStr)) {
                        return TestUnit.NONE;
                }
                for (TestUnit u : TestUnit.values()) {
                        if (u.name().equalsIgnoreCase(unitStr) || u.getSymbol().equalsIgnoreCase(unitStr)) {
                                return u;
                        }
                }
                return TestUnit.NONE;
        }
}
