package com.roms.service;

import com.roms.entity.Candidate;
import com.roms.entity.CandidateDocument;
import com.roms.enums.DocumentType;
import com.roms.repository.CandidateDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Automated Document Sufficiency Engine
 * Evaluates if candidate documents meet requirements for progression
 */
@Service
public class DocumentEvaluationService {

    @Autowired
    private CandidateDocumentRepository documentRepository;

    @Value("${roms.passport.min-validity-months:6}")
    private int passportMinValidityMonths;

    public DocumentEvaluationResult evaluateDocuments(Candidate candidate) {
        List<String> missing = new ArrayList<>();
        List<CandidateDocument> docs = documentRepository.findByCandidateId(candidate.getId());

        // RULE 1: Passport must be present
        CandidateDocument passport = docs.stream()
                .filter(d -> d.getDocType() == DocumentType.PASSPORT)
                .findFirst()
                .orElse(null);

        if (passport == null) {
            missing.add("Passport bio page");
        }

        // RULE 2: Passport expiry ≥ today + 6 months
        boolean passportValid = false;
        if (passport != null && passport.getExpiryDate() != null) {
            LocalDate minValidDate = LocalDate.now().plusMonths(passportMinValidityMonths);
            if (passport.getExpiryDate().isAfter(minValidDate)) {
                passportValid = true;
            } else {
                missing.add("Passport valid for at least " + passportMinValidityMonths + " months");
            }
        } else if (passport != null) {
            missing.add("Passport expiry date");
        }

        // RULE 3: Check for essential documents
        DocumentType[] requiredDocs = {DocumentType.CV, DocumentType.EDUCATIONAL_CERTIFICATE};

        for (DocumentType type : requiredDocs) {
            boolean found = docs.stream().anyMatch(d -> d.getDocType() == type);
            if (!found) {
                missing.add(getDocumentDisplayName(type));
            }
        }

        boolean sufficient = missing.isEmpty();
        return new DocumentEvaluationResult(sufficient, missing, passportValid);
    }

    private String getDocumentDisplayName(DocumentType type) {
        switch (type) {
            case PASSPORT: return "Passport bio page";
            case CV: return "Curriculum Vitae (CV)";
            case MEDICAL_REPORT: return "Medical certificate";
            case POLICE_CLEARANCE: return "Police clearance certificate";
            case PHOTO: return "Passport-size photograph";
            case NATIONAL_ID: return "National ID card";
            case BIRTH_CERTIFICATE: return "Birth certificate";
            case OFFER_LETTER: return "Offer letter";
            case CONTRACT: return "Employment contract";
            case VISA: return "Visa document";
            default: return type.name();
        }
    }

    public static class DocumentEvaluationResult {
        public final boolean sufficient;
        public final List<String> missingDocuments;
        public final boolean passportValid;

        public DocumentEvaluationResult(boolean sufficient, List<String> missingDocuments, boolean passportValid) {
            this.sufficient = sufficient;
            this.missingDocuments = missingDocuments;
            this.passportValid = passportValid;
        }

        public boolean isSufficient() {
            return sufficient;
        }
    }
}
