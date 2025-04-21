package com.example.amena.service;

import com.example.amena.model.Insurance;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] generateInsuranceReport(List<Insurance> insuranceList, BigDecimal lossRatio, BigDecimal solvencyRatio, BigDecimal claimSettlementSpeed) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(200, 750);
            contentStream.showText("Insurance Report");
            contentStream.endText();

            // Add KPIs Section
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Key Performance Indicators (KPIs)");
            contentStream.endText();

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 680);
            contentStream.showText("• Loss Ratio: " + lossRatio + " %");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("• Solvency Ratio: " + solvencyRatio + " %");
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText("• Claim Settlement Speed: " + claimSettlementSpeed + " days");
            contentStream.endText();

            // Table Header
            float startY = 630;
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, startY);
            contentStream.showText("User ID    Policy Type    Premium    Coverage Amount    Claim Status");
            contentStream.endText();

            // Table Data
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            for (Insurance insurance : insuranceList) {
                startY -= 15;
                contentStream.beginText();
                contentStream.newLineAtOffset(100, startY);
                contentStream.showText(
                        insurance.getUserId() + "    " +
                                insurance.getPolicyType() + "    " +
                                insurance.getPremium() + "    " +
                                insurance.getCoverageAmount() + "    " +
                                insurance.getClaimStatus()
                );
                contentStream.endText();
            }

            contentStream.close();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
