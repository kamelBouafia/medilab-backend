package com.medilab.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BaseDirection;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.layout.splitting.DefaultSplitCharacters;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.Bidi;
import com.medilab.dto.ReportGenerationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfReportService {

        private final MessageSource messageSource;

        private static final PdfFont ARABIC_FONT;
        private static final PdfFont ARABIC_BOLD_FONT;

        static {
                try {
                        byte[] fontBytes = new ClassPathResource("fonts/NotoSansArabic-VariableFont_wdth,wght.ttf")
                                        .getInputStream()
                                        .readAllBytes();
                        ARABIC_FONT = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H,
                                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                        ARABIC_BOLD_FONT = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H,
                                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                } catch (IOException e) {
                        log.error("Could not load Arabic font. Arabic PDFs will not be rendered correctly.", e);
                        throw new RuntimeException("Could not load Arabic font", e);
                }
        }

        // Professional and modern color palette
        private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(30, 136, 229); // A vibrant, professional blue
        private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(245, 245, 245); // A very light gray for
                                                                                       // backgrounds
        private static final DeviceRgb FONT_COLOR = new DeviceRgb(51, 51, 51); // Dark gray for text
        private static final DeviceRgb BORDER_COLOR = new DeviceRgb(224, 224, 224); // Light gray for borders

        // Flag colors
        private static final Color CRITICAL_FLAG_COLOR = new DeviceRgb(194, 24, 7); // A strong red
        private static final Color HIGH_LOW_FLAG_COLOR = new DeviceRgb(230, 81, 0); // A dark orange
        private static final Color ABNORMAL_FLAG_COLOR = new DeviceRgb(255, 179, 0); // Amber

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");

        public byte[] generateReport(ReportGenerationDto reportData, Locale locale) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        PdfWriter writer = new PdfWriter(baos);
                        PdfDocument pdfDoc = new PdfDocument(writer);
                        Document document = new Document(pdfDoc);
                        document.setMargins(36, 36, 36, 36);

                        boolean isRtl = locale.getLanguage().equals("ar");
                        PdfFont regularFont, boldFont;

                        if (isRtl) {
                                // For manual reordering, we do not set
                                // document.setBaseDirection(BaseDirection.RIGHT_TO_LEFT);
                                regularFont = ARABIC_FONT;
                                boldFont = ARABIC_BOLD_FONT;
                        } else {
                                regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA, PdfEncodings.WINANSI);
                                boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD,
                                                PdfEncodings.WINANSI);
                        }

                        if (isRtl) {
                                document.setFont(regularFont);
                        }

                        addHeader(document, reportData, boldFont, locale, isRtl);
                        addPatientAndRequisitionInfo(document, reportData, regularFont, boldFont, locale, isRtl);
                        addTestResults(document, reportData, regularFont, boldFont, locale, isRtl);
                        addLabAndDoctorInfo(document, reportData, regularFont, boldFont, locale, isRtl);
                        addFooter(document, regularFont, locale, isRtl);

                        document.close();
                        log.info("Generated PDF report for requisition: {}", reportData.getRequisitionId());
                        return baos.toByteArray();
                } catch (IOException e) {
                        log.error("Error generating PDF report", e);
                        throw new RuntimeException("Failed to generate PDF report", e);
                }
        }

        private void addHeader(Document document, ReportGenerationDto reportData, PdfFont boldFont, Locale locale,
                        boolean isRtl) {
                Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 1, 2 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setBorder(Border.NO_BORDER)
                                .setMarginBottom(20);

                Cell logoCell = new Cell()
                                .add(new Paragraph("MediLab")
                                                .setFont(boldFont)
                                                .setFontSize(26)
                                                .setFontColor(PRIMARY_COLOR)
                                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT)
                                                .setSplitCharacters(new DefaultSplitCharacters()))
                                .setBorder(Border.NO_BORDER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);

                Paragraph title = new Paragraph(getMessage("report.title", locale))
                                .setFont(boldFont)
                                .setFontSize(18)
                                .setFontColor(FONT_COLOR)
                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT)
                                .setTextAlignment(isRtl ? TextAlignment.RIGHT : TextAlignment.RIGHT);

                Paragraph subtitle = new Paragraph(
                                getMessage("report.requisitionId", locale) + ": "
                                                + handleDataText(String.valueOf(reportData.getRequisitionId()), isRtl))
                                .setFont(boldFont)
                                .setFontSize(10)
                                .setFontColor(ColorConstants.GRAY)
                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT)
                                .setTextAlignment(isRtl ? TextAlignment.RIGHT : TextAlignment.RIGHT);

                Cell titleCell = new Cell().add(title).add(subtitle)
                                .setBorder(Border.NO_BORDER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE);

                if (isRtl) {
                        headerTable.addCell(titleCell);
                        headerTable.addCell(logoCell);
                } else {
                        headerTable.addCell(logoCell);
                        headerTable.addCell(titleCell);
                }
                document.add(headerTable);
        }

        private void addPatientAndRequisitionInfo(Document document, ReportGenerationDto reportData,
                        PdfFont regularFont, PdfFont boldFont, Locale locale, boolean isRtl) {
                addSectionTitle(document, getMessage("report.patientDetails", locale), boldFont, isRtl);
                Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 2, 1, 2 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setMarginBottom(20);

                addInfoRow(table, getMessage("report.patientName", locale), reportData.getPatientName(), regularFont,
                                boldFont,
                                isRtl);
                addInfoRow(table, getMessage("report.dob", locale),
                                reportData.getPatientDob() != null ? reportData.getPatientDob().format(DATE_FORMATTER)
                                                : "N/A",
                                regularFont, boldFont, isRtl);
                addInfoRow(table, getMessage("report.gender", locale), reportData.getPatientGender(), regularFont,
                                boldFont,
                                isRtl);
                addInfoRow(table, getMessage("report.bloodGroup", locale),
                                reportData.getPatientBloodGroup() != null ? reportData.getPatientBloodGroup() : "N/A",
                                regularFont, boldFont, isRtl);
                addInfoRow(table, getMessage("report.phone", locale), reportData.getPatientPhone(), regularFont,
                                boldFont,
                                isRtl);
                addInfoRow(table, getMessage("report.email", locale),
                                reportData.getPatientEmail() != null ? reportData.getPatientEmail() : "N/A",
                                regularFont, boldFont, isRtl);
                addFullWidthInfoRow(table, getMessage("report.address", locale), reportData.getPatientAddress(),
                                regularFont,
                                boldFont, isRtl);
                addFullWidthInfoRow(table, getMessage("report.allergies", locale), reportData.getPatientAllergies(),
                                regularFont, boldFont, isRtl);
                addInfoRow(table, getMessage("report.requisitionDate", locale),
                                reportData.getRequisitionDate() != null
                                                ? reportData.getRequisitionDate().format(DATETIME_FORMATTER)
                                                : "N/A",
                                regularFont, boldFont, isRtl);
                addInfoRow(table, getMessage("report.completionDate", locale),
                                reportData.getCompletionDate() != null
                                                ? reportData.getCompletionDate().format(DATETIME_FORMATTER)
                                                : "N/A",
                                regularFont, boldFont, isRtl);

                document.add(table);
        }

        private void addTestResults(Document document, ReportGenerationDto reportData, PdfFont regularFont,
                        PdfFont boldFont, Locale locale, boolean isRtl) {
                addSectionTitle(document, getMessage("report.testResults", locale), boldFont, isRtl);
                Table table = new Table(UnitValue.createPercentArray(new float[] { 3, 2, 2, 2, 1, 3 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setMarginBottom(20);

                table.addHeaderCell(createHeaderCell(getMessage("report.testName", locale), boldFont, isRtl));
                table.addHeaderCell(createHeaderCell(getMessage("report.category", locale), boldFont, isRtl));
                table.addHeaderCell(createHeaderCell(getMessage("report.result", locale), boldFont, isRtl));
                table.addHeaderCell(createHeaderCell(getMessage("report.refRange", locale), boldFont, isRtl));
                table.addHeaderCell(createHeaderCell(getMessage("report.flag", locale), boldFont, isRtl));
                table.addHeaderCell(createHeaderCell(getMessage("report.interpretation", locale), boldFont, isRtl));

                boolean evenRow = false;
                for (ReportGenerationDto.TestResultDetail result : reportData.getTestResults()) {
                        table.addCell(createDataCell(result.getTestName(), regularFont, evenRow, isRtl));
                        table.addCell(createDataCell(
                                        result.getTestCategory() != null ? result.getTestCategory() : "N/A",
                                        regularFont, evenRow, isRtl));
                        table.addCell(createDataCell(result.getResultValue(), regularFont, evenRow, isRtl));
                        table.addCell(createDataCell(result.getReferenceRange(), regularFont, evenRow, isRtl));
                        table.addCell(createFlagCell(result.getFlag(), regularFont, boldFont, evenRow, isRtl));
                        table.addCell(createDataCell(
                                        result.getInterpretation() != null ? result.getInterpretation() : "N/A",
                                        regularFont, evenRow, isRtl));
                        evenRow = !evenRow;
                }

                document.add(table);
        }

        private void addLabAndDoctorInfo(Document document, ReportGenerationDto reportData, PdfFont regularFont,
                        PdfFont boldFont, Locale locale, boolean isRtl) {
                addSectionTitle(document, getMessage("report.labAndDoctorInfo", locale), boldFont, isRtl);
                Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 2, 1, 2 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setMarginBottom(20);

                addInfoRow(table, getMessage("report.labName", locale), reportData.getLabName(), regularFont, boldFont,
                                isRtl);
                addInfoRow(table, getMessage("report.doctor", locale),
                                reportData.getDoctorName() != null ? reportData.getDoctorName() : "N/A",
                                regularFont, boldFont, isRtl);
                addInfoRow(table, getMessage("report.labLocation", locale), reportData.getLabLocation(), regularFont,
                                boldFont,
                                isRtl);
                addInfoRow(table, getMessage("report.labContact", locale),
                                reportData.getLabContactEmail() != null ? reportData.getLabContactEmail() : "N/A",
                                regularFont, boldFont, isRtl);

                document.add(table);
        }

        private void addFooter(Document document, PdfFont regularFont, Locale locale, boolean isRtl) {
                Paragraph footer = new Paragraph(getMessage("report.footer.generated", locale))
                                .setFont(regularFont)
                                .setFontSize(9)
                                .setFontColor(ColorConstants.GRAY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT);
                document.add(footer);

                Paragraph generatedOn = new Paragraph(getMessage("report.footer.generatedOn", locale) + ": "
                                + java.time.LocalDateTime.now().format(DATETIME_FORMATTER))
                                .setFont(regularFont)
                                .setFontSize(9)
                                .setFontColor(ColorConstants.GRAY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT);
                document.add(generatedOn);
        }

        private void addSectionTitle(Document document, String title, PdfFont boldFont, boolean isRtl) {
                Paragraph sectionTitle = new Paragraph(title)
                                .setFont(boldFont)
                                .setFontSize(14)
                                .setFontColor(PRIMARY_COLOR)
                                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                                .setMarginBottom(10)
                                .setTextAlignment(isRtl ? TextAlignment.RIGHT : TextAlignment.LEFT);
                document.add(sectionTitle);
        }

        private void addInfoRow(Table table, String label, String value, PdfFont regularFont, PdfFont boldFont,
                        boolean isRtl) {
                table.addCell(createLabelCell(label, boldFont, isRtl));
                table.addCell(createValueCell(value, regularFont, 1, isRtl));
        }

        private void addFullWidthInfoRow(Table table, String label, String value, PdfFont regularFont,
                        PdfFont boldFont, boolean isRtl) {
                table.addCell(createLabelCell(label, boldFont, isRtl));
                table.addCell(createValueCell(value, regularFont, 3, isRtl));
        }

        private Cell createLabelCell(String text, PdfFont boldFont, boolean isRtl) {
                Paragraph p = new Paragraph(text)
                                .setFont(boldFont)
                                .setFontSize(9)
                                .setFontColor(FONT_COLOR)
                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT)
                                .setTextAlignment(isRtl ? TextAlignment.RIGHT : TextAlignment.LEFT);

                return new Cell()
                                .add(p)
                                .setBorder(Border.NO_BORDER)
                                .setPadding(5);
        }

        private Cell createValueCell(String text, PdfFont regularFont, int colspan, boolean isRtl) {
                Paragraph p = new Paragraph(handleDataText(text, isRtl))
                                .setFont(regularFont)
                                .setFontSize(9)
                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT)
                                .setTextAlignment(isRtl ? TextAlignment.RIGHT : TextAlignment.LEFT);

                return new Cell(1, colspan)
                                .add(p)
                                .setBorder(Border.NO_BORDER)
                                .setPadding(5);
        }

        private Cell createHeaderCell(String text, PdfFont boldFont, boolean isRtl) {
                Paragraph p = new Paragraph(text)
                                .setFont(boldFont)
                                .setFontSize(10)
                                .setFontColor(ColorConstants.WHITE)
                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT);

                return new Cell()
                                .add(p)
                                .setBackgroundColor(PRIMARY_COLOR)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                                .setPadding(8)
                                .setBorder(new SolidBorder(PRIMARY_COLOR, 1));
        }

        private Cell createDataCell(String text, PdfFont font, boolean isEven, boolean isRtl) {
                Paragraph p = new Paragraph(handleDataText(text, isRtl))
                                .setFont(font)
                                .setFontSize(9)
                                .setBaseDirection(BaseDirection.LEFT_TO_RIGHT)
                                .setTextAlignment(isRtl ? TextAlignment.RIGHT : TextAlignment.LEFT);

                Cell cell = new Cell()
                                .add(p)
                                .setPadding(6)
                                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                                .setBorderTop(Border.NO_BORDER);

                if (isEven) {
                        cell.setBackgroundColor(SECONDARY_COLOR);
                }

                return cell;
        }

        private Cell createFlagCell(String flag, PdfFont regularFont, PdfFont boldFont, boolean isEven, boolean isRtl) {
                Cell cell = createDataCell(flag, regularFont, isEven, isRtl);
                if (flag == null)
                        return cell;

                switch (flag) {
                        case "CRITICAL_HIGH":
                        case "CRITICAL_LOW":
                                cell.setFontColor(CRITICAL_FLAG_COLOR).setFont(boldFont);
                                break;
                        case "HIGH":
                        case "LOW":
                                cell.setFontColor(HIGH_LOW_FLAG_COLOR);
                                break;
                        case "ABNORMAL":
                                cell.setFontColor(ABNORMAL_FLAG_COLOR);
                                break;
                }
                return cell;
        }

        private String getMessage(String code, Locale locale) {
                String message = messageSource.getMessage(code, null, locale);
                if (locale.getLanguage().equals("ar")) {
                        return processArabicText(message);
                }
                return message;
        }

        private String processArabicText(String text) {
                if (text == null || text.trim().isEmpty() || text.equals("N/A")) {
                        return text;
                }
                try {
                        ArabicShaping shaping = new ArabicShaping(
                                        ArabicShaping.SHAPE_TAIL_NEW_UNICODE | ArabicShaping.LETTERS_SHAPE);
                        String shapedText = shaping.shape(text);
                        Bidi bidi = new Bidi(shapedText, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);
                        if (bidi.isLeftToRight()) {
                                return shapedText;
                        }
                        return bidi.writeReordered(Bidi.REORDER_DEFAULT);
                } catch (Exception e) {
                        log.warn("Error processing Arabic text: {}", text, e);
                        return text;
                }
        }

        private String handleDataText(String text, boolean isRtl) {
                if (text == null || text.equals("N/A"))
                        return "N/A";
                return isRtl ? processArabicText(text) : text;
        }
}