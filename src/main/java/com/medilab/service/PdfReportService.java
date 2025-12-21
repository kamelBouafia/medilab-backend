package com.medilab.service;

import com.itextpdf.io.font.constants.StandardFonts;
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
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.medilab.dto.ReportGenerationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfReportService {

    // Professional and modern color palette
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(30, 136, 229); // A vibrant, professional blue
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(245, 245, 245); // A very light gray for backgrounds
    private static final DeviceRgb FONT_COLOR = new DeviceRgb(51, 51, 51); // Dark gray for text, easier on the eyes
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(224, 224, 224); // Light gray for borders

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");

    /**
     * Generate a PDF report for medical test results
     *
     * @param reportData The data to include in the report
     * @return PDF as byte array
     */
    public byte[] generateReport(ReportGenerationDto reportData) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            document.setMargins(36, 36, 36, 36); // Add some page margins

            // Define fonts
            PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Add header
            addHeader(document, reportData, boldFont);

            // Add patient and requisition details
            addPatientAndRequisitionInfo(document, reportData, regularFont, boldFont);

            // Add test results
            addTestResults(document, reportData, regularFont, boldFont);

            // Add lab and doctor information
            addLabAndDoctorInfo(document, reportData, regularFont, boldFont);

            // Add footer
            addFooter(document, regularFont);

            document.close();
            log.info("Generated PDF report for requisition: {}", reportData.getRequisitionId());
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private void addHeader(Document document, ReportGenerationDto reportData, PdfFont boldFont) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBorder(Border.NO_BORDER)
                .setMarginBottom(20);

        // Placeholder for a logo
        Cell logoCell = new Cell()
                .add(new Paragraph("MediLab")
                        .setFont(boldFont)
                        .setFontSize(26)
                        .setFontColor(PRIMARY_COLOR))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        // Title
        Paragraph title = new Paragraph("Medical Test Report")
                .setFont(boldFont)
                .setFontSize(18)
                .setFontColor(FONT_COLOR)
                .setTextAlignment(TextAlignment.RIGHT);
        Paragraph subtitle = new Paragraph("Requisition ID: " + reportData.getRequisitionId())
                .setFont(boldFont)
                .setFontSize(10)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.RIGHT);

        Cell titleCell = new Cell().add(title).add(subtitle)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);

        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);
        document.add(headerTable);
    }

    private void addPatientAndRequisitionInfo(Document document, ReportGenerationDto reportData, PdfFont regularFont, PdfFont boldFont) {
        addSectionTitle(document, "Patient & Requisition Details", boldFont);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addInfoRow(table, "Patient Name", reportData.getPatientName(), regularFont, boldFont);
        addInfoRow(table, "Date of Birth", reportData.getPatientDob() != null ? reportData.getPatientDob().format(DATE_FORMATTER) : "N/A", regularFont, boldFont);
        addInfoRow(table, "Gender", reportData.getPatientGender(), regularFont, boldFont);
        addInfoRow(table, "Blood Group", reportData.getPatientBloodGroup() != null ? reportData.getPatientBloodGroup() : "N/A", regularFont, boldFont);
        addInfoRow(table, "Phone", reportData.getPatientPhone(), regularFont, boldFont);
        addInfoRow(table, "Email", reportData.getPatientEmail() != null ? reportData.getPatientEmail() : "N/A", regularFont, boldFont);

        // Span across two columns for address and allergies
        addFullWidthInfoRow(table, "Address", reportData.getPatientAddress(), regularFont, boldFont);
        addFullWidthInfoRow(table, "Allergies", reportData.getPatientAllergies(), regularFont, boldFont);

        addInfoRow(table, "Requisition Date", reportData.getRequisitionDate() != null ? reportData.getRequisitionDate().format(DATETIME_FORMATTER) : "N/A", regularFont, boldFont);
        addInfoRow(table, "Completion Date", reportData.getCompletionDate() != null ? reportData.getCompletionDate().format(DATETIME_FORMATTER) : "N/A", regularFont, boldFont);

        document.add(table);
    }

    private void addTestResults(Document document, ReportGenerationDto reportData, PdfFont regularFont, PdfFont boldFont) {
        addSectionTitle(document, "Test Results", boldFont);

        Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 4}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Header row
        table.addHeaderCell(createHeaderCell("Test Name", boldFont));
        table.addHeaderCell(createHeaderCell("Category", boldFont));
        table.addHeaderCell(createHeaderCell("Result", boldFont));
        table.addHeaderCell(createHeaderCell("Interpretation", boldFont));

        // Data rows with zebra striping
        boolean evenRow = false;
        for (ReportGenerationDto.TestResultDetail result : reportData.getTestResults()) {
            table.addCell(createDataCell(result.getTestName(), regularFont, evenRow));
            table.addCell(createDataCell(result.getTestCategory() != null ? result.getTestCategory() : "N/A", regularFont, evenRow));
            table.addCell(createDataCell(result.getResultValue(), regularFont, evenRow));
            table.addCell(createDataCell(result.getInterpretation() != null ? result.getInterpretation() : "N/A", regularFont, evenRow));
            evenRow = !evenRow;
        }

        document.add(table);
    }

    private void addLabAndDoctorInfo(Document document, ReportGenerationDto reportData, PdfFont regularFont, PdfFont boldFont) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        addInfoRow(table, "Lab Name", reportData.getLabName(), regularFont, boldFont);
        addInfoRow(table, "Doctor", reportData.getDoctorName() != null ? reportData.getDoctorName() : "N/A", regularFont, boldFont);
        addInfoRow(table, "Lab Location", reportData.getLabLocation(), regularFont, boldFont);
        addInfoRow(table, "Lab Contact", reportData.getLabContactEmail() != null ? reportData.getLabContactEmail() : "N/A", regularFont, boldFont);

        document.add(table);
    }

    private void addFooter(Document document, PdfFont regularFont) {
        document.add(new Paragraph("This is an electronically generated report and does not require a signature.")
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

        document.add(new Paragraph("Generated on: " + java.time.LocalDateTime.now().format(DATETIME_FORMATTER))
                .setFont(regularFont)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    // Helper methods for creating styled elements

    private void addSectionTitle(Document document, String title, PdfFont boldFont) {
        Paragraph sectionTitle = new Paragraph(title)
                .setFont(boldFont)
                .setFontSize(14)
                .setFontColor(PRIMARY_COLOR)
                .setBorderBottom(new SolidBorder(BORDER_COLOR, 1))
                .setMarginBottom(10);
        document.add(sectionTitle);
    }

    private void addInfoRow(Table table, String label, String value, PdfFont regularFont, PdfFont boldFont) {
        table.addCell(createLabelCell(label, boldFont));
        table.addCell(createValueCell(value, regularFont));
    }

    private void addFullWidthInfoRow(Table table, String label, String value, PdfFont regularFont, PdfFont boldFont) {
        table.addCell(createLabelCell(label, boldFont));
        table.addCell(createValueCell(value, regularFont, 3));
    }

    private Cell createLabelCell(String text, PdfFont boldFont) {
        return new Cell()
                .add(new Paragraph(text))
                .setFont(boldFont)
                .setFontSize(9)
                .setFontColor(FONT_COLOR)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private Cell createValueCell(String text, PdfFont regularFont) {
        return createValueCell(text, regularFont, 1);
    }

    private Cell createValueCell(String text, PdfFont regularFont, int colspan) {
        return new Cell(1, colspan)
                .add(new Paragraph(text != null ? text : "N/A"))
                .setFont(regularFont)
                .setFontSize(9)
                .setBorder(Border.NO_BORDER)
                .setPadding(5);
    }

    private Cell createHeaderCell(String text, PdfFont boldFont) {
        return new Cell()
                .add(new Paragraph(text))
                .setFont(boldFont)
                .setFontSize(10)
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(new SolidBorder(PRIMARY_COLOR, 1));
    }

    private Cell createDataCell(String text, PdfFont regularFont, boolean isEven) {
        Cell cell = new Cell()
                .add(new Paragraph(text != null ? text : "N/A"))
                .setFont(regularFont)
                .setFontSize(9)
                .setPadding(6)
                .setBorder(new SolidBorder(BORDER_COLOR, 1))
                .setBorderTop(Border.NO_BORDER);

        if (isEven) {
            cell.setBackgroundColor(SECONDARY_COLOR);
        }
        return cell;
    }
}
