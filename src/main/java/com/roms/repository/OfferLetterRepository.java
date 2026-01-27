package com.roms.repository;

import com.roms.entity.OfferLetter;
import com.roms.enums.OfferLetterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferLetterRepository extends JpaRepository<OfferLetter, Long> {
    
    /**
     * Find all offer letters for a specific candidate
     */
    @Query("SELECT o FROM OfferLetter o WHERE o.candidate.id = :candidateId AND o.deletedAt IS NULL")
    List<OfferLetter> findByCandidateId(@Param("candidateId") Long candidateId);
    
    /**
     * Find all offer letters for a specific job order
     */
    @Query("SELECT o FROM OfferLetter o WHERE o.jobOrder.id = :jobOrderId AND o.deletedAt IS NULL")
    List<OfferLetter> findByJobOrderId(@Param("jobOrderId") Long jobOrderId);
    
    /**
     * Find offer letters by status
     */
    @Query("SELECT o FROM OfferLetter o WHERE o.status = :status AND o.deletedAt IS NULL")
    List<OfferLetter> findByStatus(@Param("status") OfferLetterStatus status);
    
    /**
     * Find active (issued but not signed) offers for a candidate
     */
    @Query("SELECT o FROM OfferLetter o WHERE o.candidate.id = :candidateId " +
           "AND o.status = 'ISSUED' AND o.deletedAt IS NULL")
    List<OfferLetter> findActiveOffersByCandidateId(@Param("candidateId") Long candidateId);
    
    /**
     * Check if candidate has any pending (issued) offers
     */
    @Query("SELECT COUNT(o) > 0 FROM OfferLetter o WHERE o.candidate.id = :candidateId " +
           "AND o.status = 'ISSUED' AND o.deletedAt IS NULL")
    boolean hasPendingOffer(@Param("candidateId") Long candidateId);
    
    /**
     * Find all active offers (not deleted)
     */
    @Query("SELECT o FROM OfferLetter o WHERE o.deletedAt IS NULL")
    List<OfferLetter> findAllActive();
}
