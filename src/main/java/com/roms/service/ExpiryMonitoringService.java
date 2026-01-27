package com.roms.service;

import com.roms.entity.Candidate;
import com.roms.repository.CandidateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled service to monitor document and medical expiry dates
 * Critical for recruitment compliance and candidate readiness
 */
@Service
@Slf4j
public class ExpiryMonitoringService {

    @Autowired
    private CandidateRepository candidateRepository;

    private static final int EXPIRY_WARNING_DAYS = 90;

    /**
     * Scheduled job runs daily at 2:00 AM
     * Checks all candidates for passport and medical expiry
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void checkExpiries() {
        log.info("Starting expiry monitoring job...");
        
        LocalDate today = LocalDate.now();
        LocalDate expiryThreshold = today.plusDays(EXPIRY_WARNING_DAYS);

        // Get all active candidates
        List<Candidate> candidates = candidateRepository.findAllActive();
        
        int expiredCount = 0;
        int expiringSoonCount = 0;

        for (Candidate candidate : candidates) {
            boolean updated = false;
            String previousFlag = candidate.getExpiryFlag();

            // Check passport expiry
            if (candidate.getPassportExpiry() != null) {
                LocalDate passportExpiry = candidate.getPassportExpiry();
                
                if (passportExpiry.isBefore(today)) {
                    if (!"EXPIRED".equals(candidate.getExpiryFlag())) {
                        candidate.setExpiryFlag("EXPIRED");
                        updated = true;
                        expiredCount++;
                        log.warn("Candidate {} - Passport EXPIRED (Expiry: {})", 
                                candidate.getInternalRefNo(), passportExpiry);
                    }
                } else if (passportExpiry.isBefore(expiryThreshold)) {
                    if (!"EXPIRING_SOON".equals(candidate.getExpiryFlag())) {
                        candidate.setExpiryFlag("EXPIRING_SOON");
                        updated = true;
                        expiringSoonCount++;
                        log.warn("Candidate {} - Passport EXPIRING SOON (Expiry: {})", 
                                candidate.getInternalRefNo(), passportExpiry);
                    }
                }
            }

            // Check medical expiry
            if (candidate.getMedicalExpiry() != null) {
                LocalDate medicalExpiry = candidate.getMedicalExpiry();
                
                if (medicalExpiry.isBefore(today)) {
                    if (!"EXPIRED".equals(candidate.getExpiryFlag())) {
                        candidate.setExpiryFlag("EXPIRED");
                        updated = true;
                        expiredCount++;
                        log.warn("Candidate {} - Medical EXPIRED (Expiry: {})", 
                                candidate.getInternalRefNo(), medicalExpiry);
                    }
                } else if (medicalExpiry.isBefore(expiryThreshold)) {
                    if (!"EXPIRING_SOON".equals(candidate.getExpiryFlag())) {
                        candidate.setExpiryFlag("EXPIRING_SOON");
                        updated = true;
                        expiringSoonCount++;
                        log.warn("Candidate {} - Medical EXPIRING SOON (Expiry: {})", 
                                candidate.getInternalRefNo(), medicalExpiry);
                    }
                }
            }

            // If both passport and medical are valid (or not set)
            if (updated) {
                candidateRepository.save(candidate);
                
                // Log status change
                log.info("Candidate {} expiry status changed: {} -> {}", 
                        candidate.getInternalRefNo(), 
                        previousFlag != null ? previousFlag : "NONE", 
                        candidate.getExpiryFlag());
            }
        }

        log.info("Expiry monitoring completed. Expired: {}, Expiring Soon: {}, Total Checked: {}", 
                expiredCount, expiringSoonCount, candidates.size());
    }

    /**
     * Manual trigger for expiry check (for testing or admin use)
     */
    public void runManualExpiryCheck() {
        log.info("Manual expiry check triggered");
        checkExpiries();
    }

    /**
     * Get candidates with expiring documents
     */
    public List<Candidate> getExpiringCandidates() {
        return candidateRepository.findAllActive().stream()
                .filter(c -> "EXPIRING_SOON".equals(c.getExpiryFlag()) || "EXPIRED".equals(c.getExpiryFlag()))
                .toList();
    }
}
