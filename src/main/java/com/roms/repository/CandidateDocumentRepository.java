package com.roms.repository;

import com.roms.entity.CandidateDocument;
import com.roms.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateDocumentRepository extends JpaRepository<CandidateDocument, Long> {
    
    @Query("SELECT d FROM CandidateDocument d WHERE d.candidate.id = :candidateId AND d.deletedAt IS NULL")
    List<CandidateDocument> findByCandidateId(@Param("candidateId") Long candidateId);
    
    @Query("SELECT d FROM CandidateDocument d WHERE d.candidate.id = :candidateId AND d.docType = :docType AND d.deletedAt IS NULL")
    Optional<CandidateDocument> findByCandidateIdAndDocType(@Param("candidateId") Long candidateId, 
                                                             @Param("docType") DocumentType docType);
    
    Optional<CandidateDocument> findByDriveFileId(String driveFileId);
    
    @Query("SELECT d FROM CandidateDocument d WHERE d.isVerified = :verified AND d.deletedAt IS NULL")
    List<CandidateDocument> findByVerificationStatus(@Param("verified") Boolean verified);
}
