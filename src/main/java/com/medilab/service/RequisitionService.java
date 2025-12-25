package com.medilab.service;

import com.medilab.dto.RequisitionDto;
import com.medilab.entity.Requisition;
import com.medilab.entity.SampleStatus;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.RequisitionMapper;
import com.medilab.repository.LabRepository;
import com.medilab.repository.LabTestRepository;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.RequisitionRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.AuthenticatedUser;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import com.medilab.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.Optional;
import com.medilab.entity.TestResult;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.medilab.entity.LabTest;
import com.medilab.entity.Patient;
import com.medilab.enums.TestResultFlag;

@Service
@RequiredArgsConstructor
public class RequisitionService {

    private final RequisitionRepository requisitionRepository;
    private final PatientRepository patientRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final LabTestRepository labTestRepository;
    private final RequisitionMapper requisitionMapper;
    private final NotificationProducerService notificationProducerService;
    private final MinIOService minIOService;

    @Transactional(readOnly = true)
    public Page<RequisitionDto> getRequisitions(int page, int limit, String q, String sort, String order,
            MultiValueMap<String, String> params) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, limit, Sort.by(direction, sort));

        if (params.containsKey("patientId")) {
            List<Long> patientIds = params.get("patientId").stream().map(Long::valueOf).toList();
            if (patientIds.size() == 1) {
                return requisitionRepository
                        .findByPatientIdAndLabIdWithTestsAndResults(patientIds.get(0), user.getLabId(), pageable)
                        .map(requisitionMapper::toDto);
            }
        }

        Specification<Requisition> spec = (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("patient", JoinType.LEFT);
                root.fetch("createdBy", JoinType.LEFT);
                // Note: testResults is NOT fetched here to avoid Cartesian product.
                // The mapper will lazy-load testResults within the @Transactional context.
            }
            // Ensure distinct results if we are joining collections
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("lab").get("id"), user.getLabId()));

            if (StringUtils.hasText(q)) {
                predicates.add(cb.like(cb.lower(root.get("patient").get("name")), "%" + q.toLowerCase() + "%"));
            }

            params.forEach((key, values) -> {
                if (values == null || values.isEmpty() || values.getFirst() == null)
                    return;

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
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return requisitionRepository.findAll(spec, pageable).map(requisitionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public RequisitionDto getRequisitionById(Long id) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Optional<Requisition> requisitionOptional = requisitionRepository.findByIdAndLabIdWithTestsAndResults(id,
                user.getLabId());
        Requisition requisition = requisitionOptional
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found with id: " + id));
        return requisitionMapper.toDto(requisition);
    }

    public RequisitionDto createRequisition(RequisitionDto requisitionDto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Requisition requisition = requisitionMapper.toEntity(requisitionDto);

        patientRepository.findById(requisitionDto.getPatientId()).ifPresent(requisition::setPatient);
        labRepository.findById(user.getLabId()).ifPresent(requisition::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(requisition::setCreatedBy);

        if (requisitionDto.getTestIds() != null) {
            requisition.setTests(new HashSet<>(labTestRepository.findAllById(requisitionDto.getTestIds())));
        }

        Requisition savedRequisition = requisitionRepository.save(requisition);
        return requisitionMapper.toDto(savedRequisition);
    }

    @Transactional
    public RequisitionDto updateRequisitionStatus(Long requisitionId, RequisitionDto requisitionDto) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found with id: " + requisitionId));

        SampleStatus newStatus = SampleStatus.valueOf(requisitionDto.getStatus());

        if (requisition.getStatus() == SampleStatus.COMPLETED || requisition.getStatus() == SampleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a completed or cancelled requisition.");
        }

        requisition.setStatus(newStatus);

        if (newStatus == SampleStatus.COMPLETED) {
            requisition.setCompletionDate(LocalDateTime.now());
        }

        Requisition updatedRequisition = requisitionRepository.save(requisition);
        return requisitionMapper.toDto(updatedRequisition);
    }

    public String getReportUrl(Long requisitionId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Requisition requisition = requisitionRepository
                .findByIdAndLabIdWithTestsAndResults(requisitionId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found with id: " + requisitionId));

        if (requisition.getPdfObjectPath() == null) {
            throw new IllegalStateException("PDF report has not been generated yet for this requisition");
        }

        // Generate presigned URL on-demand from stored object path
        String bucketName = "lab-" + requisition.getLab().getId() + "-reports";
        return minIOService.getPresignedUrl(bucketName, requisition.getPdfObjectPath());
    }

    @Transactional
    public void resendReport(Long requisitionId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Requisition requisition = requisitionRepository
                .findByIdAndLabIdWithTestsAndResults(requisitionId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found with id: " + requisitionId));

        if (requisition.getPdfObjectPath() == null) {
            throw new IllegalStateException("PDF report has not been generated yet for this requisition");
        }

        // Generate fresh presigned URL for the email
        String bucketName = "lab-" + requisition.getLab().getId() + "-reports";
        String pdfUrl = minIOService.getPresignedUrl(bucketName, requisition.getPdfObjectPath());

        // Send notification email with PDF link
        com.medilab.dto.NotificationRequestDTO notification = new com.medilab.dto.NotificationRequestDTO();
        notification.setType("EMAIL");
        notification.setRecipient(requisition.getPatient().getEmail());
        notification.setSubject("Your Medical Test Results - Resent");
        notification.setContent(
                "Dear " + requisition.getPatient().getName() + ",\n\n" +
                        "As requested, here is your medical test results report for requisition #" + requisition.getId()
                        + ".\n\n" +
                        "You can download your report using the link below:\n" +
                        pdfUrl + "\n\n" +
                        "Please note: This link will expire in 7 days. The report will be available for 30 days.\n\n" +
                        "If you have any questions about your results, please consult with your healthcare provider.\n\n"
                        +
                        "Best regards,\n" +
                        requisition.getLab().getName());

        notificationProducerService.sendNotification(notification);
    }

    @Transactional(readOnly = true)
    public List<com.medilab.dto.TestResultDto> getRequisitionResults(Long requisitionId) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"));

        // Get all tests for this requisition
        Set<com.medilab.entity.LabTest> tests = requisition.getTests();
        if (tests == null) {
            return Collections.emptyList();
        }

        List<TestResult> results = requisition.getTestResults();

        // Create DTOs for all tests, with or without results
        return tests.stream()
                .map(test -> {
                    com.medilab.dto.TestResultDto dto = new com.medilab.dto.TestResultDto();

                    // Test metadata
                    dto.setTestId(test.getId());
                    dto.setTestName(test.getName());
                    dto.setTestCategory(test.getCategory().name());
                    dto.setTestPrice(test.getPrice().doubleValue());
                    dto.setTestUnit(test.getUnit() != null ? test.getUnit().name() : null);
                    dto.setTestMinVal(test.getMinVal());
                    dto.setTestMaxVal(test.getMaxVal());
                    dto.setTestCriticalMinVal(test.getCriticalMinVal());
                    dto.setTestCriticalMaxVal(test.getCriticalMaxVal());
                    dto.setRequisitionId(requisitionId);

                    // Find matching result if exists
                    if (results != null) {
                        results.stream()
                                .filter(result -> result.getTest().getId().equals(test.getId()))
                                .findFirst()
                                .ifPresent(result -> {
                                    dto.setId(result.getId());
                                    dto.setResultValue(result.getResultValue());
                                    dto.setInterpretation(result.getInterpretation());
                                    dto.setFlag(result.getFlag());
                                    dto.setEnteredById(
                                            result.getEnteredBy() != null ? result.getEnteredBy().getId() : null);
                                });
                    }

                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}
