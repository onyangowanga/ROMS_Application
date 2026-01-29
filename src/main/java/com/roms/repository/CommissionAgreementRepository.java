package com.roms.repository;

import com.roms.entity.CommissionAgreement;
import com.roms.enums.CommissionAgreementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionAgreementRepository extends JpaRepository<CommissionAgreement, Long> {

    /**
     * Find all commission agreements for a candidate
     */
    List<CommissionAgreement> findByCandidateId(Long candidateId);

    /**
     * Find active commission agreements for a candidate
     */
    @Query("SELECT ca FROM CommissionAgreement ca WHERE ca.candidate.id = :candidateId AND ca.isActive = true")
    List<CommissionAgreement> findActiveByCandidateId(@Param("candidateId") Long candidateId);

    /**
     * Find commission agreement by assignment
     */
    Optional<CommissionAgreement> findByAssignmentId(Long assignmentId);

    /**
     * Find commission agreement by agreement number
     */
    Optional<CommissionAgreement> findByAgreementNumber(String agreementNumber);

    /**
     * Find commission agreements by status
     */
    List<CommissionAgreement> findByStatus(CommissionAgreementStatus status);

    /**
     * Check if candidate has active agreement
     */
    @Query("SELECT COUNT(ca) > 0 FROM CommissionAgreement ca WHERE ca.candidate.id = :candidateId AND ca.isActive = true AND ca.status IN ('ACTIVE', 'SIGNED')")
    boolean hasActiveAgreement(@Param("candidateId") Long candidateId);
}
