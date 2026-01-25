package com.medilab.service;

import com.medilab.dto.NotificationRequestDTO;
import com.medilab.dto.ReportGenerationDto;
import com.medilab.dto.TestResultDto;
import com.medilab.entity.*;
import com.medilab.enums.TestResultFlag;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.TestResultMapper;
import com.medilab.repository.*;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final RequisitionRepository requisitionRepository;
    private final LabTestRepository labTestRepository;
    private final StaffUserRepository staffUserRepository;
    private final PdfReportService pdfReportService;
    private final MinIOService minIOService;
    private final NotificationProducerService notificationProducerService;
    private final LabRepository labRepository;
    private final TestResultMapper testResultMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public List<TestResultDto> saveTestResults(List<TestResultDto> testResultDtos) {
        if (testResultDtos.isEmpty())
            return Collections.emptyList();

        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        if (user.getParentLabId() != null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Branch labs cannot enter or edit results.");
        }
        Long requisitionId = testResultDtos.getFirst().getRequisitionId();

        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"));

        Map<Long, TestResult> existingResultsMap = testResultRepository.findByRequisitionId(requisitionId).stream()
                .collect(Collectors.toMap(tr -> tr.getTest().getId(), Function.identity()));

        Set<Long> testIds = testResultDtos.stream().map(TestResultDto::getTestId).collect(Collectors.toSet());
        Map<Long, LabTest> labTestsMap = labTestRepository.findAllById(testIds).stream()
                .collect(Collectors.toMap(LabTest::getId, Function.identity()));

        Lab lab = labRepository.getReferenceById(user.getLabId());
        StaffUser enteredBy = staffUserRepository.getReferenceById(user.getId());

        List<TestResult> resultsToSave = testResultDtos.stream()
                .map(dto -> processTestResult(dto, existingResultsMap, labTestsMap, requisition, lab, enteredBy))
                .filter(Objects::nonNull)
                .toList();

        List<TestResult> savedResults = testResultRepository.saveAll(resultsToSave);

        updateRequisitionStatusIfComplete(requisition, savedResults);

        return savedResults.stream().map(testResultMapper::toDto).toList();
    }

    private TestResult processTestResult(TestResultDto dto, Map<Long, TestResult> existingMap,
            Map<Long, LabTest> testMap, Requisition requisition,
            Lab lab, StaffUser enteredBy) {
        LabTest labTest = testMap.get(dto.getTestId());
        if (labTest == null)
            return null;

        // Ownership and Editability Check
        boolean isProvider = (labTest.getType() == com.medilab.enums.TestType.IN_HOUSE
                && requisition.getLab().getId().equals(lab.getId()))
                || (labTest.getType() == com.medilab.enums.TestType.OUTSOURCED && labTest.getPartnerLab() != null
                        && labTest.getPartnerLab().getId().equals(lab.getId()));

        if (!isProvider) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to enter results for test: " + labTest.getName());
        }

        TestResult testResult = existingMap.getOrDefault(dto.getTestId(), new TestResult());

        if (testResult.getId() != null) {
            if (testResult.getStatus() == com.medilab.enums.TestResultStatus.FINALIZED ||
                    testResult.getStatus() == com.medilab.enums.TestResultStatus.CANCELLED) {
                throw new IllegalStateException("Cannot edit a finalized or cancelled test result.");
            }
        }

        if (testResult.getId() == null) {
            testResult.setRequisition(requisition);
            testResult.setTest(labTest);
            testResult.setLab(lab);
            testResult.setEnteredBy(enteredBy);
            testResult.setStatus(com.medilab.enums.TestResultStatus.RESULT_ENTERED);
            auditLogService.logAction("TEST_RESULT_ENTERED",
                    String.format("Test '%s' result entered: %s", labTest.getName(), dto.getResultValue()));
        } else {
            if (!testResult.getResultValue().equals(dto.getResultValue())) {
                auditLogService.logAction("TEST_RESULT_UPDATED",
                        String.format("Test '%s' result changed from '%s' to '%s'",
                                labTest.getName(), testResult.getResultValue(), dto.getResultValue()));
            }
            if (dto.getStatus() != null) {
                testResult.setStatus(dto.getStatus());
            }
        }

        testResult.setResultValue(dto.getResultValue());
        testResult.setInterpretation(dto.getInterpretation());
        testResult.setFlag(dto.getFlag() != null ? dto.getFlag()
                : determineFlag(labTest, dto.getResultValue(), requisition.getPatient()));

        return testResult;
    }

    private void updateRequisitionStatusIfComplete(Requisition requisition, List<TestResult> savedResults) {
        long totalTests = requisition.getTests().size();
        long finalizedTests = testResultRepository.countByRequisitionIdAndStatus(requisition.getId(),
                com.medilab.enums.TestResultStatus.FINALIZED);

        if (totalTests == finalizedTests) {
            requisition.setStatus(SampleStatus.COMPLETED);
            requisition.setCompletionDate(LocalDateTime.now());
            requisitionRepository.save(requisition);
            log.info("Requisition {} completed. Generating PDF report.", requisition.getId());
            generateAndUploadPdfReport(requisition, savedResults);
        }
    }

    private void generateAndUploadPdfReport(Requisition requisition, List<TestResult> savedResults) {
        try {
            ReportGenerationDto reportData = buildReportData(requisition, savedResults);
            Locale locale = new Locale(requisition.getLab().getDefaultLanguage());
            byte[] pdfBytes = pdfReportService.generateReport(reportData, locale);

            String bucketName = "lab-" + requisition.getLab().getId() + "-reports";
            if (requisition.getPdfObjectPath() != null) {
                minIOService.deleteFile(bucketName, requisition.getPdfObjectPath());
            }

            String fileName = String.format("report_%d_%d.pdf", requisition.getId(), System.currentTimeMillis());
            String objectPath = minIOService.uploadPdf(bucketName, fileName, pdfBytes);

            requisition.setPdfObjectPath(objectPath);
            requisition.setPdfGeneratedAt(LocalDateTime.now());
            requisitionRepository.save(requisition);

            String baseUrl = null;
            try {
                baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            } catch (Exception e) {
                log.warn("Could not determine dynamic base URL, falling back to public endpoint.");
            }

            String pdfUrl = minIOService.getPresignedUrl(bucketName, objectPath, baseUrl);
            sendReportNotification(requisition, pdfUrl);
        } catch (Exception e) {
            log.error("Error generating PDF report for Requisition ID: {}", requisition.getId(), e);
        }
    }

    private ReportGenerationDto buildReportData(Requisition requisition, List<TestResult> savedResults) {
        List<ReportGenerationDto.TestResultDetail> details = savedResults.stream()
                .map(tr -> {
                    String referenceRange = determineReferenceRange(tr.getTest(), requisition.getPatient());
                    return ReportGenerationDto.TestResultDetail.builder()
                            .testName(tr.getTest().getName())
                            .testCategory(tr.getTest().getCategory().name())
                            .resultValue(tr.getResultValue())
                            .referenceRange(referenceRange)
                            .interpretation(tr.getInterpretation())
                            .flag(tr.getFlag() != null ? tr.getFlag().name() : null)
                            .build();
                })
                .toList();

        Patient p = requisition.getPatient();
        Lab l = requisition.getLab();

        return ReportGenerationDto.builder()
                .labName(l.getName())
                .labLocation(l.getLocation())
                .labContactEmail(l.getContactEmail())
                .labLicenseNumber(l.getLicenseNumber())
                .patientName(p.getName())
                .patientDob(p.getDob())
                .patientGender(p.getGender().name())
                .patientPhone(p.getPhone())
                .patientEmail(p.getEmail())
                .patientAddress(p.getAddress())
                .patientBloodGroup(p.getBloodGroup())
                .patientAllergies(p.getAllergies())
                .requisitionId(requisition.getId())
                .doctorName(requisition.getDoctorName())
                .requisitionDate(requisition.getDate().toLocalDateTime())
                .completionDate(requisition.getCompletionDate())
                .testResults(details)
                .build();
    }

    private void sendReportNotification(Requisition requisition, String pdfUrl) {
        NotificationRequestDTO notification = new NotificationRequestDTO();
        notification.setType("EMAIL");
        notification.setRecipient(requisition.getPatient().getEmail());
        notification.setSubject("Your Medical Test Results are Ready");
        notification.setContent(String.format(
                "Dear %s,\n\nYour medical test results for requisition #%d are now ready.\n\nDownload link:\n%s\n\nBest regards,\n%s",
                requisition.getPatient().getName(), requisition.getId(), pdfUrl, requisition.getLab().getName()));

        notificationProducerService.sendNotification(notification);
    }

    private String determineReferenceRange(LabTest test, Patient patient) {
        String unit = test.getUnit() != null ? " " + test.getUnit().getSymbol() : "";

        if (test.getMinVal() != null || test.getMaxVal() != null) {
            return String.format("Normal: %s - %s%s",
                    test.getMinVal() != null ? test.getMinVal() : "N/A",
                    test.getMaxVal() != null ? test.getMaxVal() : "N/A", unit);
        }

        if (test.getReferenceRanges() == null || test.getReferenceRanges().isEmpty()) {
            return "N/A";
        }

        int age = Period.between(patient.getDob(), LocalDate.now()).getYears();
        Patient.Gender gender = patient.getGender();

        return test.getReferenceRanges().stream()
                .filter(r -> (r.getGender() == null || r.getGender().name().equals(gender.name())))
                .filter(r -> (r.getMinAge() == null || age >= r.getMinAge()))
                .filter(r -> (r.getMaxAge() == null || age <= r.getMaxAge()))
                .findFirst()
                .map(r -> {
                    StringBuilder sb = new StringBuilder();
                    if (r.getMinVal() != null || r.getMaxVal() != null) {
                        sb.append(String.format("Normal: %s - %s%s",
                                r.getMinVal() != null ? r.getMinVal() : "N/A",
                                r.getMaxVal() != null ? r.getMaxVal() : "N/A", unit));
                    }
                    if (r.getAbnormalMin() != null || r.getAbnormalMax() != null) {
                        if (sb.length() > 0)
                            sb.append("\n");
                        sb.append(String.format("Abnormal: <%s or >%s%s",
                                r.getAbnormalMin() != null ? r.getAbnormalMin() : "N/A",
                                r.getAbnormalMax() != null ? r.getAbnormalMax() : "N/A", unit));
                    }
                    if (r.getCriticalMin() != null || r.getCriticalMax() != null) {
                        if (sb.length() > 0)
                            sb.append("\n");
                        sb.append(String.format("Critical: <%s or >%s%s",
                                r.getCriticalMin() != null ? r.getCriticalMin() : "N/A",
                                r.getCriticalMax() != null ? r.getCriticalMax() : "N/A", unit));
                    }
                    return sb.length() > 0 ? sb.toString() : "N/A";
                })
                .orElse("N/A");
    }

    private TestResultFlag determineFlag(LabTest test, String resultValue, Patient patient) {
        if (resultValue == null)
            return null;

        try {
            double value = Double.parseDouble(resultValue);

            if (test.getMinVal() != null || test.getMaxVal() != null) {
                if (test.getMaxVal() != null && value > test.getMaxVal())
                    return TestResultFlag.HIGH;
                if (test.getMinVal() != null && value < test.getMinVal())
                    return TestResultFlag.LOW;
                return TestResultFlag.NORMAL;
            }

            if (test.getReferenceRanges() == null || test.getReferenceRanges().isEmpty())
                return TestResultFlag.NORMAL;

            int age = Period.between(patient.getDob(), LocalDate.now()).getYears();
            Patient.Gender gender = patient.getGender();

            return test.getReferenceRanges().stream()
                    .filter(r -> (r.getGender() == null || r.getGender().name().equals(gender.name())))
                    .filter(r -> (r.getMinAge() == null || age >= r.getMinAge()))
                    .filter(r -> (r.getMaxAge() == null || age <= r.getMaxAge()))
                    .findFirst()
                    .map(r -> {
                        if (r.getCriticalMax() != null && value > r.getCriticalMax())
                            return TestResultFlag.CRITICAL_HIGH;
                        if (r.getCriticalMin() != null && value < r.getCriticalMin())
                            return TestResultFlag.CRITICAL_LOW;
                        if (r.getAbnormalMax() != null && value > r.getAbnormalMax())
                            return TestResultFlag.HIGH;
                        if (r.getAbnormalMin() != null && value < r.getAbnormalMin())
                            return TestResultFlag.LOW;
                        if (r.getMaxVal() != null && value > r.getMaxVal())
                            return TestResultFlag.HIGH;
                        if (r.getMinVal() != null && value < r.getMinVal())
                            return TestResultFlag.LOW;
                        return TestResultFlag.NORMAL;
                    })
                    .orElse(TestResultFlag.NORMAL);
        } catch (NumberFormatException e) {
            return TestResultFlag.NORMAL;
        }
    }
}
