package com.roms.service;

import com.roms.entity.Candidate;
import com.roms.entity.JobOrder;
import com.roms.entity.OfferLetter;
import com.roms.enums.MedicalStatus;
import com.roms.enums.OfferLetterStatus;
import com.roms.exception.BusinessValidationException;
import com.roms.repository.CandidateRepository;
import com.roms.repository.JobOrderRepository;
import com.roms.repository.OfferLetterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing offer letter lifecycle
 * Implements critical business rules for offer issuance and acceptance
 */
@Service
@Slf4j
public class OfferLetterService {

    @Autowired
    private OfferLetterRepository offerLetterRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobOrderRepository jobOrderRepository;

    /**
     * Create a draft offer letter
     * Can be created without medical clearance
     */
    @Transactional
    public OfferLetter createDraft(Long candidateId, Long jobOrderId, Double salary, String jobTitle) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new BusinessValidationException("Candidate not found"));

        JobOrder jobOrder = jobOrderRepository.findById(jobOrderId)
                .orElseThrow(() -> new BusinessValidationException("Job order not found"));

        OfferLetter offerLetter = OfferLetter.builder()
                .candidate(candidate)
                .jobOrder(jobOrder)
                .status(OfferLetterStatus.DRAFT)
                .offeredSalary(salary)
                .jobTitle(jobTitle)
                .build();

        OfferLetter saved = offerLetterRepository.save(offerLetter);
        log.info("Draft offer letter created for candidate {}", candidate.getInternalRefNo());
        
        return saved;
    }

    /**
     * Issue an offer letter to a candidate
     * CRITICAL BUSINESS RULE: Requires medical clearance (PASSED status)
     * Interview is optional and can be skipped
     */
    @Transactional
    public OfferLetter issueOffer(Long offerLetterId) {
        OfferLetter offerLetter = offerLetterRepository.findById(offerLetterId)
                .orElseThrow(() -> new BusinessValidationException("Offer letter not found"));

        if (offerLetter.getStatus() != OfferLetterStatus.DRAFT) {
            throw new BusinessValidationException("Can only issue offers in DRAFT status");
        }

        Candidate candidate = offerLetter.getCandidate();

        // CRITICAL GUARD: Medical clearance is REQUIRED
        if (candidate.getMedicalStatus() == null || candidate.getMedicalStatus() != MedicalStatus.PASSED) {
            throw new BusinessValidationException(
                    "Cannot issue offer: Candidate must have PASSED medical status. Current status: " +
                    (candidate.getMedicalStatus() != null ? candidate.getMedicalStatus() : "NOT_SET")
            );
        }

        // Interview is optional - we don't check for interview completion
        // This allows fast-tracking exceptional candidates

        offerLetter.setStatus(OfferLetterStatus.ISSUED);
        offerLetter.setIssuedAt(LocalDateTime.now());

        OfferLetter issued = offerLetterRepository.save(offerLetter);
        log.info("Offer letter {} issued to candidate {}", issued.getId(), candidate.getInternalRefNo());

        return issued;
    }

    /**
     * Sign an offer letter
     * CRITICAL BUSINESS RULE: Only users with APPLICANT role can sign
     * (Enforced at controller level via @PreAuthorize)
     */
    @Transactional
    public OfferLetter signOffer(Long offerLetterId, String applicantUsername) {
        OfferLetter offerLetter = offerLetterRepository.findById(offerLetterId)
                .orElseThrow(() -> new BusinessValidationException("Offer letter not found"));

        if (offerLetter.getStatus() != OfferLetterStatus.ISSUED) {
            throw new BusinessValidationException("Can only sign offers in ISSUED status");
        }

        // Verify the applicant is the actual candidate (prevent signing someone else's offer)
        Candidate candidate = offerLetter.getCandidate();
        if (!candidate.getEmail().equalsIgnoreCase(applicantUsername)) {
            throw new BusinessValidationException("You can only sign your own offer letter");
        }

        offerLetter.setStatus(OfferLetterStatus.SIGNED);
        offerLetter.setSignedAt(LocalDateTime.now());

        OfferLetter signed = offerLetterRepository.save(offerLetter);
        log.info("Offer letter {} signed by candidate {}", signed.getId(), candidate.getInternalRefNo());

        return signed;
    }

    /**
     * Withdraw an offer letter
     */
    @Transactional
    public OfferLetter withdrawOffer(Long offerLetterId, String reason) {
        OfferLetter offerLetter = offerLetterRepository.findById(offerLetterId)
                .orElseThrow(() -> new BusinessValidationException("Offer letter not found"));

        if (offerLetter.getStatus() == OfferLetterStatus.SIGNED) {
            throw new BusinessValidationException("Cannot withdraw a signed offer");
        }

        offerLetter.setStatus(OfferLetterStatus.WITHDRAWN);
        offerLetter.setNotes(offerLetter.getNotes() != null ? 
                offerLetter.getNotes() + "\nWithdrawal reason: " + reason : 
                "Withdrawal reason: " + reason);

        OfferLetter withdrawn = offerLetterRepository.save(offerLetter);
        log.warn("Offer letter {} withdrawn. Reason: {}", withdrawn.getId(), reason);

        return withdrawn;
    }

    /**
     * Get all offer letters for a candidate
     */
    public List<OfferLetter> getCandidateOffers(Long candidateId) {
        return offerLetterRepository.findByCandidateId(candidateId);
    }

    /**
     * Get all offers for a job order
     */
    public List<OfferLetter> getJobOrderOffers(Long jobOrderId) {
        return offerLetterRepository.findByJobOrderId(jobOrderId);
    }

    /**
     * Check if candidate can receive a new offer
     * Prevents issuing multiple concurrent offers to the same candidate
     */
    public boolean canReceiveOffer(Long candidateId) {
        return !offerLetterRepository.hasPendingOffer(candidateId);
    }
}
