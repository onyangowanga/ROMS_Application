package com.roms.repository;

import com.roms.entity.Assignment;
import com.roms.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /**
     * Find active assignment for a candidate
     * Returns the most recent one if multiple exist (should not happen due to constraint)
     */
    @Query("SELECT a FROM Assignment a WHERE a.candidate.id = :candidateId AND a.isActive = true ORDER BY a.assignedAt DESC")
    List<Assignment> findActiveByCandidateId(@Param("candidateId") Long candidateId);

    /**
     * Find all assignments for a candidate (active and inactive)
     */
    List<Assignment> findByCandidateId(Long candidateId);

    /**
     * Find all assignments for a job order
     */
    List<Assignment> findByJobOrderId(Long jobOrderId);

    /**
     * Find all active assignments for a job order
     */
    @Query("SELECT a FROM Assignment a WHERE a.jobOrder.id = :jobOrderId AND a.isActive = true")
    List<Assignment> findActiveByJobOrderId(@Param("jobOrderId") Long jobOrderId);

    /**
     * Count active assignments for a job order
     */
    @Query("SELECT COUNT(a) FROM Assignment a WHERE a.jobOrder.id = :jobOrderId AND a.isActive = true")
    Long countActiveByJobOrderId(@Param("jobOrderId") Long jobOrderId);

    /**
     * Check if candidate has active assignment
     */
    @Query("SELECT COUNT(a) > 0 FROM Assignment a WHERE a.candidate.id = :candidateId AND a.isActive = true")
    boolean hasActiveAssignment(@Param("candidateId") Long candidateId);

    /**
     * Find assignments by status
     */
    List<Assignment> findByStatus(AssignmentStatus status);

    /**
     * Find active assignments by status
     */
    @Query("SELECT a FROM Assignment a WHERE a.status = :status AND a.isActive = true")
    List<Assignment> findActiveByStatus(@Param("status") AssignmentStatus status);
}
