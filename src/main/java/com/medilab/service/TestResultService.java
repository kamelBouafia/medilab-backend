package com.medilab.service;

import com.medilab.dto.TestResultDto;
import com.medilab.entity.Requisition;
import com.medilab.entity.SampleStatus;
import com.medilab.entity.TestResult;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.TestResultMapper;
import com.medilab.repository.LabRepository;
import com.medilab.repository.LabTestRepository;
import com.medilab.repository.RequisitionRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.repository.TestResultRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period; 
import java.time.LocalDate; 
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.medilab.entity.LabTest; 
import com.medilab.entity.Patient; 
import com.medilab.enums.TestResultFlag; 
import com.medilab.entity.Lab; 
import com.medilab.entity.StaffUser; 
import java.util.Set; 

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
        if (testResultDtos.isEmpty()) {
            return List.of();
        }

        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Long requisitionId = testResultDtos.getFirst().getRequisitionId();

        // Fetch all necessary data in bulk to avoid N+1 queries
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"));
        
        Map<Long, TestResult> existingResultsMap = testResultRepository.findByRequisitionId(requisitionId).stream()
                .collect(Collectors.toMap(tr -> tr.getTest().getId(), Function.identity()));

        Set<Long> testIds = testResultDtos.stream().map(TestResultDto::getTestId).collect(Collectors.toSet());
        Map<Long, LabTest> labTestsMap = labTestRepository.findByIdInWithReferenceRanges(testIds).stream()
                .collect(Collectors.toMap(LabTest::getId, Function.identity()));

        Lab lab = labRepository.findById(user.getLabId()).orElseThrow(() -> new ResourceNotFoundException("Lab not found"));
        StaffUser enteredBy = staffUserRepository.findById(user.getId()).orElseThrow(() -> new ResourceNotFoundException("StaffUser not found"));


        List<TestResult> resultsToSave = testResultDtos.stream().map(dto -> {
            TestResult testResult = existingResultsMap.get(dto.getTestId());
            LabTest labTest = labTestsMap.get(dto.getTestId());

            if (labTest == null) {
                // Or throw an exception, depending on desired behavior
                return null; 
            }

            if (testResult == null) {
                // This is a new result
                testResult = new TestResult();
                testResult.setRequisition(requisition);
                testResult.setTest(labTest);
                testResult.setLab(lab);
                testResult.setEnteredBy(enteredBy);
            }

            // Audit differences
            if (testResult.getId() != null) {
                if (!testResult.getResultValue().equals(dto.getResultValue())) {
                    auditLogService.logAction("TEST_RESULT_UPDATED",
                            "Test '" + labTest.getName() + "' result changed from '"
                                    + testResult.getResultValue() + "' to '" + dto.getResultValue() + "'");
                }
            } else {
                auditLogService.logAction("TEST_RESULT_ENTERED",
                        "Test '" + labTest.getName() + "' result entered: " + dto.getResultValue());
            }

            // Update values
            testResult.setResultValue(dto.getResultValue());
            testResult.setInterpretation(dto.getInterpretation());

            // Allow manual override from FE
            if (dto.getFlag() != null) {
                testResult.setFlag(dto.getFlag());
            } else {
                testResult.setFlag(determineFlag(labTest, dto.getResultValue(), requisition.getPatient()));
            }

            return testResult;
        }).filter(java.util.Objects::nonNull) // Filter out nulls if a labTest wasn't found
        .collect(Collectors.toList());

        // Use a batch save operation
        List<TestResult> savedTestResults = testResultRepository.saveAll(resultsToSave);

        // Check if all tests for this requisition now have results
        long totalTests = requisition.getTests().size();
        long completedTests = testResultRepository.countByRequisitionId(requisitionId);

        if (totalTests == completedTests) {
            // All tests are complete, update status and generate PDF
            requisition.setStatus(SampleStatus.COMPLETED);
            requisition.setCompletionDate(LocalDateTime.now());
            requisitionRepository.save(requisition);

            // Generate and upload PDF report
            generateAndUploadPdfReport(requisition);
        }

        return savedTestResults.stream()
                .map(testResultMapper::toDto)
                .collect(Collectors.toList());
    }

    private void generateAndUploadPdfReport(Requisition requisition) {
        try {
            // Build report data
            com.medilab.dto.ReportGenerationDto reportData = buildReportData(requisition);

            // Generate PDF
            byte[] pdfBytes = pdfReportService.generateReport(reportData);

            // Delete existing PDF if present
            // We need the bucket name. Assuming standardized naming: lab-{id}-reports
            String bucketName = "lab-" + requisition.getLab().getId() + "-reports";

            if (requisition.getPdfObjectPath() != null) {
                minIOService.deleteFile(bucketName, requisition.getPdfObjectPath());
            }

            // Upload to MinIO
            String fileName = "report_" + requisition.getId() + "_" + System.currentTimeMillis() + ".pdf";
            String objectPath = minIOService.uploadPdf(bucketName, fileName, pdfBytes);

            // Store object path in requisition (not the presigned URL)
            requisition.setPdfObjectPath(objectPath);
            requisition.setPdfGeneratedAt(LocalDateTime.now());
            requisitionRepository.save(requisition);

            // Generate presigned URL for email notification
            String pdfUrl = minIOService.getPresignedUrl(bucketName, objectPath);

            // Send email notification
            sendReportNotification(requisition, pdfUrl);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Error generating PDF report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private com.medilab.dto.ReportGenerationDto buildReportData(Requisition requisition) {
        List<com.medilab.dto.ReportGenerationDto.TestResultDetail> testResults = requisition.getTestResults().stream()
                .map(tr -> com.medilab.dto.ReportGenerationDto.TestResultDetail.builder()
                        .testName(tr.getTest().getName())
                        .testCategory(tr.getTest().getCategory().name())
                        .resultValue(tr.getResultValue())
                        .referenceRange(determineReferenceRange(tr.getTest(), requisition.getPatient()))
                        .interpretation(tr.getInterpretation())
                        .flag(tr.getFlag() != null ? tr.getFlag().name() : null)
                        .build())
                .collect(Collectors.toList());

        return com.medilab.dto.ReportGenerationDto.builder()
                .labName(requisition.getLab().getName())
                .labLocation(requisition.getLab().getLocation())
                .labContactEmail(requisition.getLab().getContactEmail())
                .labLicenseNumber(requisition.getLab().getLicenseNumber())
                .patientName(requisition.getPatient().getName())
                .patientDob(requisition.getPatient().getDob())
                .patientGender(requisition.getPatient().getGender().name())
                .patientPhone(requisition.getPatient().getPhone())
                .patientEmail(requisition.getPatient().getEmail())
                .patientAddress(requisition.getPatient().getAddress())
                .patientBloodGroup(requisition.getPatient().getBloodGroup())
                .patientAllergies(requisition.getPatient().getAllergies())
                .requisitionId(requisition.getId())
                .doctorName(requisition.getDoctorName())
                .requisitionDate(requisition.getDate().toLocalDateTime())
                .completionDate(requisition.getCompletionDate())
                .testResults(testResults)
                .build();
    }

    private void sendReportNotification(Requisition requisition, String pdfUrl) {
        com.medilab.dto.NotificationRequestDTO notification = new com.medilab.dto.NotificationRequestDTO();
        notification.setType("EMAIL");
        notification.setRecipient(requisition.getPatient().getEmail());
        notification.setSubject("Your Medical Test Results are Ready");
        notification.setContent(
                "Dear " + requisition.getPatient().getName() + ",\n\n" +
                        "Your medical test results for requisition #" + requisition.getId() + " are now ready.\n\n" +
                        "You can download your report using the link below:\n" +
                        pdfUrl + "\n\n" +
                        "Please note: This link will expire in 7 days. The report will be available for 30 days.\n\n" +
                        "If you have any questions about your results, please consult with your healthcare provider.\n\n"
                        +
                        "Best regards,\n" +
                        requisition.getLab().getName());

        notificationProducerService.sendNotification(notification);
    }

    private String determineReferenceRange(LabTest test, Patient patient) {
        // Priority 1: Simple Min/Max on LabTest
        if (test.getMinVal() != null || test.getMaxVal() != null) {
            String unit = test.getUnit() != null ? " " + test.getUnit().getSymbol() : ""; // Use getSymbol() for Enum
            String min = test.getMinVal() != null ? String.valueOf(test.getMinVal()) : "?";
            String max = test.getMaxVal() != null ? String.valueOf(test.getMaxVal()) : "?";
            return min + " - " + max + unit;
        }

        // Priority 2: Legacy Reference Ranges
        if (test.getReferenceRanges() == null || test.getReferenceRanges().isEmpty()) {
            return "N/A";
        }

        int age = Period.between(patient.getDob(), LocalDate.now()).getYears();
        Patient.Gender gender = patient.getGender();

        return test.getReferenceRanges().stream()
                .filter(range -> (range.getGender() == null || range.getGender().name().equals(gender.name())))
                .filter(range -> (range.getMinAge() == null || age >= range.getMinAge()))
                .filter(range -> (range.getMaxAge() == null || age <= range.getMaxAge()))
                .findFirst()
                .map(range -> {
                    String unit = test.getUnit() != null ? " " + test.getUnit() : "";
                    return range.getMinVal() + " - " + range.getMaxVal() + unit;
                })
                .orElse("N/A");
    }

    private TestResultFlag determineFlag(LabTest test, String resultValue, Patient patient) {
        if (resultValue == null)
            return null;

        try {
            double value = Double.parseDouble(resultValue);

            // Priority 1: Simple Min/Max on LabTest
            if (test.getMinVal() != null || test.getMaxVal() != null) {
                if (test.getMaxVal() != null && value > test.getMaxVal())
                    return TestResultFlag.HIGH;
                if (test.getMinVal() != null && value < test.getMinVal())
                    return TestResultFlag.LOW;
                return TestResultFlag.NORMAL;
            }

            // Priority 2: Legacy Reference Ranges
            if (test.getReferenceRanges() == null || test.getReferenceRanges().isEmpty()) {
                return TestResultFlag.NORMAL; // or null
            }

            int age = Period.between(patient.getDob(), LocalDate.now()).getYears();
            Patient.Gender gender = patient.getGender();

            return test.getReferenceRanges().stream()
                    .filter(range -> (range.getGender() == null || range.getGender().name().equals(gender.name())))
                    .filter(range -> (range.getMinAge() == null || age >= range.getMinAge()))
                    .filter(range -> (range.getMaxAge() == null || age <= range.getMaxAge()))
                    .findFirst()
                    .map(range -> {
                        // Check Critical
                        if (range.getCriticalMax() != null && value > range.getCriticalMax())
                            return TestResultFlag.CRITICAL_HIGH;
                        if (range.getCriticalMin() != null && value < range.getCriticalMin())
                            return TestResultFlag.CRITICAL_LOW;

                        // Check Abnormal
                        if (range.getAbnormalMax() != null && value > range.getAbnormalMax())
                            return TestResultFlag.HIGH; // Using HIGH for abnormal max
                        if (range.getAbnormalMin() != null && value < range.getAbnormalMin())
                            return TestResultFlag.LOW; // Using LOW for abnormal min

                        // Check Normal Range (Explicit High/Low if outside normal but inside
                        // abnormal/critical boundaries or if abnormal not defined)
                        if (range.getMaxVal() != null && value > range.getMaxVal())
                            return TestResultFlag.HIGH;
                        if (range.getMinVal() != null && value < range.getMinVal())
                            return TestResultFlag.LOW;

                        return TestResultFlag.NORMAL;
                    })
                    .orElse(TestResultFlag.NORMAL);

        } catch (NumberFormatException e) {
            // Not a number, cannot determine flag automatically
            return TestResultFlag.NORMAL;
        }
    }
}
