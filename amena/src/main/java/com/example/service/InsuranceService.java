package com.example.amena.service;

import com.example.amena.model.Insurance;
import com.example.amena.repository.InsuranceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.math.RoundingMode;



@Service
public class InsuranceService {

    @Autowired
    private InsuranceRepository insuranceRepository;

    @Transactional
    public Insurance createInsurance(Insurance insurance) {
        return insuranceRepository.save(insurance);
    }

    public List<Insurance> getAllInsurance() {
        return insuranceRepository.findAll();
    }

    public Optional<Insurance> getInsuranceById(int id) {
        return insuranceRepository.findById(id);
    }

    @Transactional
    public Optional<Insurance> updateInsurance(int id, Insurance updatedInsurance) {
        return insuranceRepository.findById(id)
                .map(existingInsurance -> {
                    updatedInsurance.setId(id); // Ensure the ID is set to the existing one
                    return insuranceRepository.save(updatedInsurance);
                });
    }

    @Transactional
    public boolean deleteInsurance(int id) {
        if (insuranceRepository.existsById(id)) {
            insuranceRepository.deleteById(id);
            return true;
        }
        return false;
    }
    public double getClaimsRejectionRatio() {
        long totalClaims = insuranceRepository.countTotalClaims();
        long rejectedClaims = insuranceRepository.countRejectedClaims();

        if (totalClaims == 0) {
            return 0.0; // Avoid division by zero
        }

        return (double) rejectedClaims / totalClaims * 100; // Convert to percentage
    }



    public BigDecimal getLossRatio() {
        BigDecimal totalClaimsPaid = insuranceRepository.getTotalClaimsPaid();
        BigDecimal totalPremiumsCollected = insuranceRepository.getTotalPremiumsCollected();

        if (totalPremiumsCollected.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // Avoid division by zero
        }

        return totalClaimsPaid.divide(totalPremiumsCollected, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getSolvencyRatio() {
        BigDecimal totalAssets = insuranceRepository.getTotalAdmissibleAssets();
        BigDecimal totalLiabilities = insuranceRepository.getTotalLiabilities();

        if (totalLiabilities.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // Avoid division by zero
        }

        return totalAssets.divide(totalLiabilities, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getClaimSettlementSpeed() {
        long totalProcessingTime = insuranceRepository.getTotalClaimProcessingTime();
        long totalSettledClaims = insuranceRepository.getNumberOfSettledClaims();

        if (totalSettledClaims == 0) {
            return BigDecimal.ZERO; // Avoid division by zero
        }

        return BigDecimal.valueOf((double) totalProcessingTime / totalSettledClaims);
    }

    public List<Insurance> searchInsurance(BigDecimal minCoverageAmount, BigDecimal maxCoverageAmount, BigDecimal minPremium, BigDecimal maxPremium) {
        // If no parameters are provided, return all records
        if (minCoverageAmount == null && maxCoverageAmount == null && minPremium == null && maxPremium == null) {
            return insuranceRepository.findAll();
        }

        return insuranceRepository.findInsuranceWithFilters( minCoverageAmount, maxCoverageAmount, minPremium, maxPremium);
    }
}