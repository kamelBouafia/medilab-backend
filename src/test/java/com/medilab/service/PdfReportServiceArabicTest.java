package com.medilab.service;

import com.medilab.dto.ReportGenerationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.medilab.config.TestContainersConfig.class)
public class PdfReportServiceArabicTest {

    @Autowired
    private PdfReportService pdfReportService;

    @Test
    void testGenerateArabicReport() {
        ReportGenerationDto reportData = ReportGenerationDto.builder()
                .labName("مختبر التجربة")
                .labLocation("الجزائر")
                .labContactEmail("lab@example.com")
                .patientName("محمد علي")
                .patientDob(LocalDate.of(1990, 1, 1))
                .patientGender("MALE")
                .requisitionId(12345L)
                .requisitionDate(LocalDateTime.now())
                .completionDate(LocalDateTime.now())
                .testResults(Collections.singletonList(
                        ReportGenerationDto.TestResultDetail.builder()
                                .testName("فحص سكر الدم")
                                .testCategory("BIOCHEMISTRY")
                                .resultValue("1.1")
                                .referenceRange("0.7 - 1.1")
                                .interpretation("طبيعي")
                                .build()))
                .build();

        byte[] pdfBytes = pdfReportService.generateReport(reportData, Locale.forLanguageTag("ar"));

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);

        // Note: Full PDF content verification for Arabic characters would require
        // a more complex PDF parser that understands iText's layout.
        // For now, if this completes without Exception, it means the font
        // loading and basic rendering of Arabic strings (from properties and DTO)
        // worked.
    }
}
