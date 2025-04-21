package com.example.amena.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private InsuranceService insuranceService;

    public void sendInsuranceReport(String recipientEmail) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(recipientEmail);
        helper.setSubject("Insurance Report");
        helper.setText("Please find the attached insurance report.");

        // Generate PDF Report
        byte[] pdfData = pdfExportService.generateInsuranceReport(
                insuranceService.getAllInsurance(),
                insuranceService.getLossRatio(),
                insuranceService.getSolvencyRatio(),
                insuranceService.getClaimSettlementSpeed()
        );

        // Attach PDF to email
        helper.addAttachment("insurance_report.pdf", () -> new ByteArrayInputStream(pdfData));

        mailSender.send(message);
    }
}
