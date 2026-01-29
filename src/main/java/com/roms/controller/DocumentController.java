package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.entity.Candidate;
import com.roms.entity.CandidateDocument;
import com.roms.entity.User;
import com.roms.enums.DocumentType;
import com.roms.repository.CandidateDocumentRepository;
import com.roms.repository.CandidateRepository;
import com.roms.repository.UserRepository;
import com.roms.service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DocumentController {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateDocumentRepository documentRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoogleDriveService driveService;

    @Autowired(required = false)
    private com.roms.service.LocalFileStorageService localFileStorageService;

    @PostMapping("/candidates/{candidateId}/documents")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long candidateId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") DocumentType docType,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) String expiryDate,
            Authentication authentication) {

        try {
            // Validate candidate exists
            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + candidateId));

            // Security check: If user is authenticated and is APPLICANT, ensure they own this candidate record
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                if (authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_APPLICANT"))) {
                    String username = authentication.getName();
                    // Find the user by username and get their email
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if (userOpt.isPresent()) {
                        String userEmail = userOpt.get().getEmail();
                        // Check if the candidate's email matches the logged-in user's email
                        if (candidate.getEmail() == null || !candidate.getEmail().equals(userEmail)) {
                            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                    .body(ApiResponse.error("You can only upload documents to your own application"));
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error("User not found"));
                    }
                }
            }
            // If authentication is null or anonymous, allow upload (for registration process)

            // Upload to Google Drive or local storage
            String driveFileId;
            
            if (localFileStorageService != null) {
                // Use local file storage for development
                driveFileId = localFileStorageService.storeFile(file);
            } else {
                // Use Google Drive
                driveFileId = driveService.uploadFile(file, null);
            }

            // Create document metadata
            CandidateDocument document = new CandidateDocument();
            document.setCandidate(candidate);
            document.setDocType(docType);  // Use docType field
            document.setDocumentNumber(documentNumber);
            document.setDriveFileId(driveFileId);
            document.setFileName(file.getOriginalFilename());
            document.setFileSize(file.getSize());
            document.setContentType(file.getContentType());  // Use contentType field
            // description field is optional
            
            if (expiryDate != null && !expiryDate.isEmpty()) {
                document.setExpiryDate(LocalDate.parse(expiryDate));
            }

            CandidateDocument savedDocument = documentRepository.save(document);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Document uploaded successfully", savedDocument));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload document: " + e.getMessage()));
        }
    }

    @GetMapping("/candidates/{candidateId}/documents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER', 'APPLICANT')")
    public ResponseEntity<?> getCandidateDocuments(@PathVariable Long candidateId, Authentication authentication) {
        
        // Validate candidate exists
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + candidateId));

        // Security check: If user is APPLICANT, ensure they own this candidate record
        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_APPLICANT"))) {
            String username = authentication.getName();
            // Find the user by username and get their email
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                String userEmail = userOpt.get().getEmail();
                if (candidate.getEmail() == null || !candidate.getEmail().equals(userEmail)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("You can only view documents for your own application"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("User not found"));
            }
        }

        List<CandidateDocument> documents = documentRepository.findByCandidateId(candidateId);  // Use existing method
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", documents));
    }

    @GetMapping("/documents/{documentId}/download")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'APPLICANT')")
    public ResponseEntity<?> downloadDocument(@PathVariable Long documentId, Authentication authentication) {
        
        try {
            // Get document metadata
            CandidateDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Security check: If user is APPLICANT, ensure they own this document
            if (authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_APPLICANT"))) {
                String username = authentication.getName();
                // Find the user by username and get their email
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    String userEmail = userOpt.get().getEmail();
                    Candidate candidate = document.getCandidate();
                    if (candidate.getEmail() == null || !candidate.getEmail().equals(userEmail)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.error("You can only download your own documents"));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("User not found"));
                }
            }

            // Download from Google Drive (backend streams file - no direct Drive URLs exposed)
            InputStream inputStream = driveService.downloadFile(document.getDriveFileId());
            byte[] fileData = inputStream.readAllBytes();

            // Prepare resource
            Resource resource = new ByteArrayResource(fileData);

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getFileName() + "\"");
            headers.setContentType(MediaType.parseMediaType(document.getContentType()));  // Use contentType field

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileData.length)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to download document: " + e.getMessage()));
        }
    }

    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteDocument(@PathVariable Long documentId) {
        
        try {
            // Get document metadata
            CandidateDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Delete from storage (Google Drive or local)
            if (localFileStorageService != null) {
                localFileStorageService.deleteFile(document.getDriveFileId());
            } else {
                driveService.deleteFile(document.getDriveFileId());
            }

            // Delete from database
            documentRepository.delete(document);

            return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete document: " + e.getMessage()));
        }
    }

    @GetMapping("/documents/{documentId}/share")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> getShareableLink(@PathVariable Long documentId) {
        
        try {
            // Get document metadata
            CandidateDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

            // Return existing shareable link (no direct Drive URLs exposed - always go through backend)
            String link = "/api/documents/" + documentId + "/download";
            
            return ResponseEntity.ok(ApiResponse.success("Shareable link generated", 
                    new ShareableLink(link, document.getFileName(), document.getDocType())));  // Use docType field

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to generate link: " + e.getMessage()));
        }
    }

    // Inner class for shareable link response
    public static class ShareableLink {
        private String downloadUrl;
        private String fileName;
        private DocumentType documentType;

        public ShareableLink(String downloadUrl, String fileName, DocumentType documentType) {
            this.downloadUrl = downloadUrl;
            this.fileName = fileName;
            this.documentType = documentType;
        }

        // Getters
        public String getDownloadUrl() { return downloadUrl; }
        public String getFileName() { return fileName; }
        public DocumentType getDocumentType() { return documentType; }
    }
}
