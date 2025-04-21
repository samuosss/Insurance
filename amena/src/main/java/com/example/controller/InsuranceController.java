package com.example.amena.controller;

import com.example.amena.model.Insurance;
import com.example.amena.service.InsuranceService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.amena.service.PdfExportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.amena.service.EmailService;
import com.example.amena.service.FraudDetectionService;
import java.io.IOException;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;



@RestController
@RequestMapping("/api/insurance")
public class InsuranceController {

    @Autowired
    private InsuranceService insuranceService;
    @Autowired
    private PdfExportService pdfExportService;
    @Autowired
    private EmailService emailService;

    private  FraudDetectionService fraudDetectionService;


    public InsuranceController(InsuranceService insuranceService, PdfExportService pdfExportService,FraudDetectionService fraudDetectionService) {
        this.insuranceService = insuranceService;
        this.pdfExportService = pdfExportService;
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping("/insurance")
    public ResponseEntity<Insurance> createInsurance(@RequestBody Insurance insurance) {
        if (insurance.getPolicyType() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        Insurance savedInsurance = insuranceService.createInsurance(insurance);
        return ResponseEntity.ok(savedInsurance);
    }


    @GetMapping
    public ResponseEntity<List<Insurance>> getAllInsurance() {
        return ResponseEntity.ok(insuranceService.getAllInsurance());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Insurance> getInsuranceById(@PathVariable int id) {
        Optional<Insurance> insurance = insuranceService.getInsuranceById(id);
        return insurance.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Insurance> updateInsurance(@PathVariable int id, @RequestBody Insurance updatedInsurance) {
        Optional<Insurance> updated = insuranceService.updateInsurance(id, updatedInsurance);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInsurance(@PathVariable int id) {
        boolean isDeleted = insuranceService.deleteInsurance(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<Object> claimInsurance(@PathVariable int id, @RequestParam BigDecimal claimAmount) {
        Optional<Insurance> insuranceOptional = insuranceService.getInsuranceById(id);

        if (insuranceOptional.isPresent()) {
            Insurance insurance = insuranceOptional.get();

            try {
                // Ensure claim does not exceed coverage amount
                if (claimAmount.compareTo(insurance.getCoverageAmount()) > 0) {
                    return ResponseEntity.badRequest().body("Claim amount exceeds coverage.");
                }

                // Process the claim
                insurance.claimInsurance(claimAmount);

                // Save the updated insurance entity with new claim status
                Insurance updatedInsurance = insuranceService.createInsurance(insurance);
                return ResponseEntity.ok(updatedInsurance);  // Return the updated insurance details

            } catch (IllegalStateException | IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Claim validation failed: " + e.getMessage());  // More informative error
            }
        } else {
            return ResponseEntity.notFound().build();  // Return 404 if insurance is not found
        }
    }

    //Key Performance Indicators
    @GetMapping("/claims/rejection-ratio")
    public ResponseEntity<Double> getClaimsRejectionRatio() {
        double rejectionRatio = insuranceService.getClaimsRejectionRatio();
        return ResponseEntity.ok(rejectionRatio);
    }

    @GetMapping("/loss-ratio")
    public ResponseEntity<BigDecimal> getLossRatio() {
        return ResponseEntity.ok(insuranceService.getLossRatio());
    }

    @GetMapping("/solvency-ratio")
    public ResponseEntity<BigDecimal> getSolvencyRatio() {
        return ResponseEntity.ok(insuranceService.getSolvencyRatio());
    }

    @GetMapping("/claim-settlement-speed")
    public ResponseEntity<BigDecimal> getClaimSettlementSpeed() {
        return ResponseEntity.ok(insuranceService.getClaimSettlementSpeed());
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportInsurancePdf() {
        try {
            // Fetch insurance data
            List<Insurance> insuranceList = insuranceService.getAllInsurance();

            // Fetch KPIs (use default values if null to prevent errors)
            BigDecimal lossRatio = insuranceService.getLossRatio() != null ? insuranceService.getLossRatio() : BigDecimal.ZERO;
            BigDecimal solvencyRatio = insuranceService.getSolvencyRatio() != null ? insuranceService.getSolvencyRatio() : BigDecimal.ZERO;
            BigDecimal claimSettlementSpeed = insuranceService.getClaimSettlementSpeed() != null ? insuranceService.getClaimSettlementSpeed() : BigDecimal.ZERO;

            // Generate PDF report with KPI metrics
            byte[] pdfData = pdfExportService.generateInsuranceReport(insuranceList, lossRatio, solvencyRatio, claimSettlementSpeed);

            // Set response headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "insurance_kpi_report.pdf");

            return ResponseEntity.ok().headers(headers).body(pdfData);

        } catch (IOException e) {
            // Log error and return server error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            // Catch any unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/send-report")
    public ResponseEntity<String> sendInsuranceReport(@RequestParam String email) {
        try {
            emailService.sendInsuranceReport(email);
            return ResponseEntity.ok("Insurance report sent successfully to " + email);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email.");
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchInsurance(
            @RequestParam(required = false) BigDecimal minCoverageAmount,
            @RequestParam(required = false) BigDecimal maxCoverageAmount,
            @RequestParam(required = false) BigDecimal minPremium,
            @RequestParam(required = false) BigDecimal maxPremium) {

        try {
            List<Insurance> insurances = insuranceService.searchInsurance(
                    minCoverageAmount, maxCoverageAmount, minPremium, maxPremium);
            return ResponseEntity.ok(insurances);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/classify")
    public String runPythonScript(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "No file uploaded.";
        }
        try {
            // Save the uploaded file to a temporary location
            File tempFile = File.createTempFile("uploaded_", ".jpg");
            file.transferTo(tempFile);

            // Run the Python script with the image path
            ProcessBuilder pb = new ProcessBuilder("py", "C:\\Users\\samim\\Desktop\\amena\\src\\main\\java\\com\\example\\amena\\python\\script.py", tempFile.getAbsolutePath());
            Process process = pb.start();

            // Read output from Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // Wait for the process to complete
            process.waitFor();

            // Delete temporary file after processing
            tempFile.delete();

            // The output is expected to be in JSON format, so we can directly return it as the response
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error running Python script: " + e.getMessage();
        }
    }



    private static final Map<String, Map<String, Object>> CLAIM_MAPPING = new HashMap<>() {{
        put("Crack", Map.of("claimType", "Windshield Replacement", "price", 550.00));
        put("Scratch", Map.of("claimType", "Paint Repair", "price", 150.00));
        put("Tire Flat", Map.of("claimType", "Tire Replacement", "price", 200.00));
        put("Dent", Map.of("claimType", "Bodywork Repair", "price", 500.00));
        put("Glass Shatter", Map.of("claimType", "Full Glass Replacement", "price", 800.00));
        put("Lamp Broken", Map.of("claimType", "Headlight/Taillight Repair", "price", 480.00));
    }};

    // ✅ Predict claim based on damage category
    @GetMapping("/predict-claim")
    public ResponseEntity<?> predictClaim(@RequestParam(required = false) String damageCategory) {
        Map<String, Object> response = new HashMap<>();

        if (damageCategory == null || damageCategory.isEmpty()) {
            response.put("error", "Missing or empty damageCategory parameter");
            return ResponseEntity.badRequest().body(response);
        }

        if (CLAIM_MAPPING.containsKey(damageCategory)) {
            // Retrieve claim details
            Map<String, Object> claimDetails = CLAIM_MAPPING.get(damageCategory);

            // Add extracted values to response
            response.put("damageCategory", damageCategory);
            response.put("claimType", claimDetails.get("claimType"));
            response.put("price", claimDetails.get("price"));

            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Unknown damage category");
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/predict")
    public String predict(@RequestBody String text) {
        return fraudDetectionService.predictFraud(text);
    }
}




