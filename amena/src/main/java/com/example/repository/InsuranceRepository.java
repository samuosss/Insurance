package com.example.amena.repository;

import com.example.amena.model.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Integer> {

    @Query("SELECT COUNT(i) FROM Insurance i WHERE i.claimStatus = 'REJECTED'")
    long countRejectedClaims();

    @Query("SELECT COUNT(i) FROM Insurance i WHERE i.claimStatus IS NOT NULL")
    long countTotalClaims();
    @Query("SELECT COALESCE(SUM(i.claimAmount), 0) FROM Insurance i WHERE i.claimStatus = 'CLAIMED'")
    BigDecimal getTotalClaimsPaid();

    @Query("SELECT COALESCE(SUM(i.premium), 0) FROM Insurance i")
    BigDecimal getTotalPremiumsCollected();

    @Query("SELECT COUNT(i) FROM Insurance i WHERE i.claimStatus = 'CLAIMED'")
    long getNumberOfSettledClaims();

    @Query("SELECT COALESCE(SUM(DATEDIFF(i.claimDate, i.startDate)), 0) FROM Insurance i WHERE i.claimStatus = 'CLAIMED'")
    long getTotalClaimProcessingTime();

    @Query("SELECT COALESCE(SUM(i.coverageAmount), 0) FROM Insurance i")
    BigDecimal getTotalAdmissibleAssets();

    @Query("SELECT COALESCE(SUM(i.claimAmount), 0) FROM Insurance i WHERE i.claimStatus = 'PENDING' OR i.claimStatus = 'CLAIMED'")
    BigDecimal getTotalLiabilities();
    @Query("SELECT i FROM Insurance i WHERE "
            + "(COALESCE(:minCoverageAmount, 0) <= i.coverageAmount) AND "
            + "(COALESCE(:maxCoverageAmount, 9999999999) >= i.coverageAmount) AND "
            + "(COALESCE(:minPremium, 0) <= i.premium) AND "
            + "(COALESCE(:maxPremium, 9999999999) >= i.premium)")
    List<Insurance> findInsuranceWithFilters(
            BigDecimal minCoverageAmount,
            BigDecimal maxCoverageAmount,
            BigDecimal minPremium,
            BigDecimal maxPremium);
}
