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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Transactional
    public List<TestResultDto> saveTestResults(List<TestResultDto> testResultDtos) {
        if (testResultDtos.isEmpty()) {
            return List.of();
        }

        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Long requisitionId = testResultDtos.get(0).getRequisitionId();

        // Fetch existing results for this requisition
        Map<Long, TestResult> existingResultsMap = testResultRepository.findByRequisitionId(requisitionId).stream()
                .collect(Collectors.toMap(tr -> tr.getTest().getId(), Function.identity()));

        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found"));

        List<TestResult> resultsToSave = testResultDtos.stream().map(dto -> {
            TestResult testResult = existingResultsMap.get(dto.getTestId());
            if (testResult == null) {
                // This is a new result
                testResult = new TestResult();
                testResult.setRequisition(requisition);
                labTestRepository.findById(dto.getTestId()).ifPresent(testResult::setTest);
                labRepository.findById(user.getLabId()).ifPresent(testResult::setLab);
                staffUserRepository.findById(user.getId()).ifPresent(testResult::setEnteredBy);
            }

            // Update values
            testResult.setResultValue(dto.getResultValue());
            testResult.setInterpretation(dto.getInterpretation());

            return testResult;
        }).collect(Collectors.toList());

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

            // Upload to MinIO
            String fileName = "report_" + requisition.getId() + "_" + System.currentTimeMillis() + ".pdf";
            String objectPath = minIOService.uploadPdf(fileName, pdfBytes);

            // Store object path in requisition (not the presigned URL)
            requisition.setPdfObjectPath(objectPath);
            requisition.setPdfGeneratedAt(LocalDateTime.now());
            requisitionRepository.save(requisition);

            // Generate presigned URL for email notification
            String pdfUrl = minIOService.getPresignedUrl(objectPath);

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
                        .testCategory(tr.getTest().getCategory())
                        .resultValue(tr.getResultValue())
                        .interpretation(tr.getInterpretation())
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
}
