package com.roms.repository;

import com.roms.entity.AgencyCommissionAgreement;
import com.roms.enums.AgreementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgencyCommissionAgreementRepository extends JpaRepository<AgencyCommissionAgreement, UUID> {

    /**
     * Find active agreement for an assignment
     */
    @Query("SELECT a FROM AgencyCommissionAgreement a WHERE a.assignment.id = :assignmentId AND a.status = 'ACTIVE'")
    Optional<AgencyCommissionAgreement> findActiveByAssignmentId(@Param("assignmentId") Long assignmentId);

    /**
     * Find all agreements for a candidate
     */
    @Query("SELECT a FROM AgencyCommissionAgreement a WHERE a.candidate.id = :candidateId ORDER BY a.agreementDate DESC")
    List<AgencyCommissionAgreement> findByCandidateId(@Param("candidateId") Long candidateId);

    /**
     * Find agreements by status
     */
    List<AgencyCommissionAgreement> findByStatus(AgreementStatus status);

    /**
     * Check if candidate has active agreement
     */
    @Query("SELECT COUNT(a) > 0 FROM AgencyCommissionAgreement a WHERE a.candidate.id = :candidateId AND a.status = 'ACTIVE'")
    boolean hasActiveAgreement(@Param("candidateId") Long candidateId);
}
