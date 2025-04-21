package com.example.amena.model;

import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Table(name = "insurance_policy") // Renamed table to avoid conflicts
@NoArgsConstructor // Lombok annotation to generate a no-args constructor
public class Insurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyType policyType;

    @Column(nullable = false, precision = 10, scale = 2) // Use BigDecimal for money
    private BigDecimal premium;

    @Column(nullable = false, precision = 12, scale = 2) // Use BigDecimal for money
    private BigDecimal coverageAmount;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date startDate;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date endDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClaimStatus claimStatus = ClaimStatus.PENDING;  // Default status is PENDING

    @Temporal(TemporalType.TIMESTAMP)
    private Date claimDate;  // Date when the claim was made

    @Column(precision = 12, scale = 2)
    private BigDecimal claimAmount;


    public Insurance(int userId, PolicyType policyType, BigDecimal premium, BigDecimal coverageAmount, Date startDate, Date endDate) {
        this.userId = userId;
        this.policyType = policyType;
        this.premium = premium;
        this.coverageAmount = coverageAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public PolicyType getPolicyType() {
        return policyType;
    }

    public void setPolicyType(PolicyType policyType) {
        this.policyType = policyType;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public BigDecimal getCoverageAmount() {
        return coverageAmount;
    }

    public void setCoverageAmount(BigDecimal coverageAmount) {
        this.coverageAmount = coverageAmount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    // Business Logic Methods
    public void subscribePolicy() {
        // Business logic for subscribing to a policy
    }

    public void claimInsurance(BigDecimal claimAmount) {
        Date currentDate = new Date();


        // Ensure claimStatus is initialized
        if (this.claimStatus == null) {
            this.claimStatus = ClaimStatus.PENDING;
        }

        // Check if the insurance is already claimed
        if (this.claimStatus.equals(ClaimStatus.CLAIMED)) {
            throw new IllegalStateException("This policy has already been claimed.");
        }

        // Validate if the claim amount exceeds the coverage amount
        if (claimAmount.compareTo(this.coverageAmount) > 0) {
            this.claimStatus = ClaimStatus.REJECTED; // Mark as rejected
            throw new IllegalArgumentException("Claim amount cannot exceed coverage amount.");
        }

        // Check if the policy has expired
        if (currentDate.after(this.endDate)) {
            throw new IllegalStateException("The insurance policy has expired.");
        }

        // Check if the policy has started yet
        if (currentDate.before(this.startDate)) {
            throw new IllegalStateException("The insurance policy has not started yet.");
        }

        // Set the claim details
        this.claimStatus = ClaimStatus.CLAIMED; // Mark the insurance as claimed
        this.claimDate = currentDate; // Set the claim date to the current date
        this.claimAmount = claimAmount; // Set the claimed amount
        this.coverageAmount = this.coverageAmount.subtract(claimAmount); // Reduce coverage amount
    }
    private static final Map<String, String> CLAIM_MAPPING = new HashMap<>() {{
        put("Crack", "Windshield Replacement");
        put("Scratch", "Paint Repair");
        put("Tire Flat", "Tire Replacement");
        put("Dent", "Bodywork Repair");
        put("Glass Shatter", "Full Glass Replacement");
        put("Lamp Broken", "Headlight/Taillight Repair");
    }};

}
