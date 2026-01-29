package com.roms.repository;

import com.roms.entity.Candidate;
import com.roms.enums.CandidateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByInternalRefNo(String internalRefNo);
    
    @Query("SELECT c FROM Candidate c WHERE c.passportNo = :passportNo AND c.deletedAt IS NULL")
    Optional<Candidate> findActiveByPassportNo(@Param("passportNo") String passportNo);
    
    List<Candidate> findByCurrentStatus(CandidateStatus status);
    
    @Query("SELECT c FROM Candidate c WHERE c.deletedAt IS NULL")
    List<Candidate> findAllActive();
    
    Optional<Candidate> findByEmailAndDeletedAtIsNull(String email);
    
    List<Candidate> findAllByEmailAndDeletedAtIsNull(String email);
    
    boolean existsByPassportNoAndDeletedAtIsNull(String passportNo);
}
