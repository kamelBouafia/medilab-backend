package com.medilab.service;

import com.medilab.dto.NotificationRequestDTO;
import com.medilab.dto.RequisitionDto;
import com.medilab.dto.TestResultDto;
import com.medilab.entity.LabTest;
import com.medilab.entity.Requisition;
import com.medilab.entity.SampleStatus;
import com.medilab.entity.TestResult;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.RequisitionMapper;
import com.medilab.mapper.TestResultMapper;
import com.medilab.repository.*;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.SecurityUtils;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequisitionService {

    private final RequisitionRepository requisitionRepository;
    private final PatientRepository patientRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final LabTestRepository labTestRepository;
    private final RequisitionMapper requisitionMapper;
    private final TestResultMapper testResultMapper;
    private final NotificationProducerService notificationProducerService;
    private final MinIOService minIOService;

    @Transactional(readOnly = true)
    public Page<RequisitionDto> getRequisitions(int page, int limit, String q, String sort, String order,
            MultiValueMap<String, String> params) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(direction, sort));

        if (params.containsKey("patientId")) {
            try {
                Long patientId = Long.valueOf(params.getFirst("patientId"));
                return requisitionRepository
                        .findByPatientIdAndHierarchicalLabIdWithTestsAndResults(patientId, user.getLabId(), pageable)
                        .map(requisitionMapper::toDto);
            } catch (NumberFormatException ignored) {
            }
        }

        Specification<Requisition> spec = createSpecification(user, q, params);
        return requisitionRepository.findAll(spec, pageable).map(requisitionMapper::toDto);
    }

    private Specification<Requisition> createSpecification(AuthenticatedUser user, String q,
            MultiValueMap<String, String> params) {
        return (root, query, cb) -> {
            if (query.getResultType().equals(Requisition.class)) {
                root.fetch("patient", JoinType.LEFT);
                root.fetch("createdBy", JoinType.LEFT);
            }
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            if (user.getParentLabId() == null) {
                // User is in a Main Lab: see own lab + all branch labs
                predicates.add(cb.or(
                        cb.equal(root.get("lab").get("id"), user.getLabId()),
                        cb.equal(root.get("lab").get("parentLab").get("id"), user.getLabId())));
            } else {
                // User is in a Branch Lab: see only their own lab's data
                predicates.add(cb.equal(root.get("lab").get("id"), user.getLabId()));
            }

            if (StringUtils.hasText(q)) {
                predicates.add(cb.like(cb.lower(root.get("patient").get("name")), "%" + q.toLowerCase() + "%"));
            }

            applyFilters(params, root, predicates, cb);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void applyFilters(MultiValueMap<String, String> params, jakarta.persistence.criteria.Root<Requisition> root,
            List<Predicate> predicates, jakarta.persistence.criteria.CriteriaBuilder cb) {
        params.forEach((key, values) -> {
            if (values == null || values.isEmpty() || values.getFirst() == null)
                return;
            try {
                switch (key) {
                    case "status_ne":
                        for (String value : values) {
                            try {
                                predicates.add(cb.notEqual(root.get("status"), SampleStatus.valueOf(value)));
                            } catch (IllegalArgumentException e) {
                                // Ignore invalid status values
                            }
                        }
                        break;
                    case "id":
                        predicates.add(root.get("id").in(values.stream().map(Long::valueOf).toList()));
                        break;
                    case "patientId":
                        predicates.add(root.get("patient").get("id").in(values.stream().map(Long::valueOf).toList()));
                        break;
                    case "createdById":
                        predicates.add(root.get("createdBy").get("id").in(values.stream().map(Long::valueOf).toList()));
                        break;
                    case "status":
                        predicates.add(root.get("status").in(values.stream().map(SampleStatus::valueOf).toList()));
                        break;
                    case "testId":
                        // Filter by Test ID - requires joining tests
                        if (values != null && !values.isEmpty()) {
                            List<Long> testIds = values.stream().map(Long::valueOf).toList();
                            predicates.add(root.join("tests").get("id").in(testIds));
                        }
                        break;
                    case "fromDate":
                        if (values.getFirst() != null) {
                            try {
                                LocalDate fromDate = LocalDate.parse(values.getFirst());
                                predicates.add(cb.greaterThanOrEqualTo(root.get("date"),
                                        fromDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC)));
                            } catch (DateTimeParseException e) {
                                // Ignore invalid date
                            }
                        }
                        break;
                    case "toDate":
                        if (values.getFirst() != null) {
                            try {
                                LocalDate toDate = LocalDate.parse(values.getFirst());
                                predicates.add(cb.lessThanOrEqualTo(root.get("date"),
                                        toDate.atTime(LocalTime.MAX).atOffset(java.time.ZoneOffset.UTC)));
                            } catch (DateTimeParseException e) {
                                // Ignore invalid date
                            }
                        }
                        break;
                }
            } catch (Exception ignored) {
                log.warn("Failed to apply filter for key: {} with values: {}", key, values);
            }
        });
    }

    @Transactional(readOnly = true)
    public RequisitionDto getRequisitionById(Long id) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        return requisitionRepository.findByIdAndHierarchicalLabIdWithTestsAndResults(id, user.getLabId())
                .map(requisitionMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found with id: " + id));
    }

    @Transactional
    public RequisitionDto createRequisition(RequisitionDto requisitionDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Requisition requisition = requisitionMapper.toEntity(requisitionDto);

        requisition.setPatient(patientRepository.getReferenceById(requisitionDto.getPatientId()));
        requisition.setLab(labRepository.getReferenceById(user.getLabId()));
        requisition.setCreatedBy(staffUserRepository.getReferenceById(user.getId()));

        if (requisitionDto.getTestIds() != null) {
            requisition.setTests(new HashSet<>(labTestRepository.findAllById(requisitionDto.getTestIds())));
        }

        Requisition savedRequisition = requisitionRepository.save(requisition);
        log.info("Requisition created: ID {}", savedRequisition.getId());
        return requisitionMapper.toDto(savedRequisition);
    }

    @Transactional
    public RequisitionDto updateRequisitionStatus(Long requisitionId,
            @NotNull(message = "Status cannot be null") SampleStatus newStatus) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found with id: " + requisitionId));

        if (requisition.getStatus() == SampleStatus.COMPLETED || requisition.getStatus() == SampleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a completed or cancelled requisition.");
        }

        requisition.setStatus(newStatus);
        if (newStatus == SampleStatus.COMPLETED) {
            requisition.setCompletionDate(LocalDateTime.now());
        }

        log.info("Requisition {} status updated to {}", requisitionId, newStatus);
        return requisitionMapper.toDto(requisitionRepository.save(requisition));
    }

    public String getReportUrl(Long requisitionId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Requisition requisition = requisitionRepository.findByIdAndHierarchicalLabId(requisitionId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"));

        if (requisition.getPdfObjectPath() == null) {
            throw new IllegalStateException("PDF report has not been generated yet");
        }

        String bucketName = "lab-" + requisition.getLab().getId() + "-reports";
        return minIOService.getPresignedUrl(bucketName, requisition.getPdfObjectPath());
    }

    @Transactional
    public void resendReport(Long requisitionId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Requisition requisition = requisitionRepository.findByIdAndHierarchicalLabId(requisitionId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"));

        if (requisition.getPdfObjectPath() == null) {
            throw new IllegalStateException("PDF report not found");
        }

        String bucketName = "lab-" + requisition.getLab().getId() + "-reports";
        String pdfUrl = minIOService.getPresignedUrl(bucketName, requisition.getPdfObjectPath());

        NotificationRequestDTO notification = new NotificationRequestDTO();
        notification.setType("EMAIL");
        notification.setRecipient(requisition.getPatient().getEmail());
        notification.setSubject("Your Medical Test Results - Resent");
        notification.setContent(String.format(
                "Dear %s,\n\nHere is your medical test results report for requisition #%d.\n\nDownload link:\n%s\n\nBest regards,\n%s",
                requisition.getPatient().getName(), requisition.getId(), pdfUrl, requisition.getLab().getName()));

        notificationProducerService.sendNotification(notification);
        log.info("Resent report for Requisition ID {}", requisitionId);
    }

    @Transactional(readOnly = true)
    public List<TestResultDto> getRequisitionResults(Long requisitionId) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"));

        Set<LabTest> tests = requisition.getTests();
        if (tests == null)
            return Collections.emptyList();

        Map<Long, TestResult> resultsMap = Optional.ofNullable(requisition.getTestResults())
                .orElse(Collections.emptyList()).stream()
                .collect(Collectors.toMap(r -> r.getTest().getId(), r -> r));

        return tests.stream()
                .map(test -> {
                    TestResult result = resultsMap.get(test.getId());
                    if (result != null) {
                        return testResultMapper.toDto(result);
                    }
                    return TestResultDto.builder()
                            .requisitionId(requisitionId)
                            .testId(test.getId())
                            .testName(test.getName())
                            .testCategory(test.getCategory().name())
                            .testPrice(test.getPrice().doubleValue())
                            .testUnit(test.getUnit() != null ? test.getUnit().name() : null)
                            .testMinVal(test.getMinVal())
                            .testMaxVal(test.getMaxVal())
                            .testCriticalMinVal(test.getCriticalMinVal())
                            .testCriticalMaxVal(test.getCriticalMaxVal())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
