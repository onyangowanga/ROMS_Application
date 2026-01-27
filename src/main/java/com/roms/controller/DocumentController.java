package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.entity.Candidate;
import com.roms.entity.CandidateDocument;
import com.roms.enums.DocumentType;
import com.roms.repository.CandidateDocumentRepository;
import com.roms.repository.CandidateRepository;
import com.roms.service.GoogleDriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateDocumentRepository documentRepository;

    @Autowired
    private GoogleDriveService driveService;

    @PostMapping("/candidates/{candidateId}/documents")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long candidateId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("docType") DocumentType docType,
            @RequestParam(required = false) String documentNumber,
            @RequestParam(required = false) String expiryDate) {

        try {
            // Validate candidate exists
            Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + candidateId));

            // Upload to Google Drive
            String driveFileId = driveService.uploadFile(file, null);
            
            // Generate shareable link (not stored in entity)
            String shareableLink = driveService.generateShareableLink(driveFileId);

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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<?> getCandidateDocuments(@PathVariable Long candidateId) {
        
        // Validate candidate exists
        if (!candidateRepository.existsById(candidateId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Candidate not found"));
        }

        List<CandidateDocument> documents = documentRepository.findByCandidateId(candidateId);  // Use existing method
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", documents));
    }

    @GetMapping("/documents/{documentId}/download")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> downloadDocument(@PathVariable Long documentId) {
        
        try {
            // Get document metadata
            CandidateDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Document not found"));

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

            // Delete from Google Drive
            driveService.deleteFile(document.getDriveFileId());

            // Soft delete from database
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
