package com.roms.service;

import com.roms.dto.JobApplicationRequest;
import com.roms.entity.Candidate;
import com.roms.entity.JobOrder;
import com.roms.entity.User;
import com.roms.enums.CandidateStatus;
import com.roms.enums.JobOrderStatus;
import com.roms.enums.MedicalStatus;
import com.roms.enums.UserRole;
import com.roms.repository.CandidateDocumentRepository;
import com.roms.repository.CandidateRepository;
import com.roms.repository.JobOrderRepository;
import com.roms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobApplicationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobOrderRepository jobOrderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CandidateDocumentRepository documentRepository;

    @Transactional
    public Candidate applyForJob(JobApplicationRequest request) {
        // Validate username doesn't exist
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // Validate email doesn't exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Validate passport number doesn't exist
        if (candidateRepository.existsByPassportNoAndDeletedAtIsNull(request.getPassportNo())) {
            throw new RuntimeException("Candidate with this passport number already exists");
        }

        // Validate job order exists and is OPEN
        JobOrder jobOrder = jobOrderRepository.findById(request.getJobOrderId())
                .orElseThrow(() -> new RuntimeException("Job order not found"));

        if (jobOrder.getStatus() != JobOrderStatus.OPEN) {
            throw new RuntimeException("Job order is not open for applications");
        }

        // Create user account with APPLICANT role
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFirstName() + " " + request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.APPLICANT)
                .isEmailVerified(false)
                .build();

        userRepository.save(user);

        // Generate internal reference number
        String internalRefNo = "CND" + System.currentTimeMillis();

        // Create candidate record
        Candidate candidate = Candidate.builder()
                .internalRefNo(internalRefNo)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .passportNo(request.getPassportNo())
                .passportExpiry(request.getPassportExpiry())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getCurrentAddress())
                .country(request.getCountry())
                .currentStatus(CandidateStatus.APPLIED)
                .medicalStatus(MedicalStatus.PENDING)
                .expectedPosition(request.getExpectedPosition())
                .build();

        // Save candidate first to get ID
        candidate = candidateRepository.save(candidate);

        // Auto-check for required documents and set appropriate status
        CandidateStatus initialStatus = determineInitialStatus(candidate);
        candidate.setCurrentStatus(initialStatus);

        return candidateRepository.save(candidate);
    }

    /**
     * Determine initial status based on uploaded documents
     */
    private CandidateStatus determineInitialStatus(Candidate candidate) {
        // Check if all required documents are present
        boolean hasPassport = documentRepository
            .findByCandidateIdAndDocType(candidate.getId(), com.roms.enums.DocumentType.PASSPORT)
            .isPresent();
        
        boolean hasPhoto = documentRepository
            .findByCandidateIdAndDocType(candidate.getId(), com.roms.enums.DocumentType.PASSPORT)
            .isPresent();
        
        boolean hasCV = documentRepository
            .findByCandidateIdAndDocType(candidate.getId(), com.roms.enums.DocumentType.CV)
            .isPresent();

        // If all required documents are present, set to DOCUMENTS_UNDER_REVIEW
        if (hasPassport && hasPhoto && hasCV) {
            return CandidateStatus.DOCUMENTS_UNDER_REVIEW;
        }

        // Otherwise, set to DOCUMENTS_PENDING
        return CandidateStatus.DOCUMENTS_PENDING;
    }
}
