package com.roms.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Service for Google Drive document storage operations
 * Handles file upload, download, deletion, and shareable link generation
 */
@Service
@Slf4j
public class GoogleDriveService {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    
    @Value("${google.drive.application-name}")
    private String applicationName;
    
    @Value("${google.drive.credentials-file-path}")
    private String credentialsFilePath;
    
    @Value("${google.drive.folder-id}")
    private String folderId;
    
    @Value("${google.drive.shared-link-duration:300}")
    private int sharedLinkDuration;
    
    private Drive driveService;
    
    /**
     * Initialize Google Drive service with credentials
     */
    @PostConstruct
    public void init() {
        log.info("Initializing Google Drive Service...");
        
        // Skip initialization if credentials file doesn't exist
        java.io.File credFile = new java.io.File(credentialsFilePath);
        if (!credFile.exists()) {
            log.warn("Google Drive credentials file not found at: {}. Service will be disabled.", credentialsFilePath);
            log.warn("To enable Google Drive integration, please configure credentials.json. See LOCAL_TESTING_GUIDE.md for instructions.");
            return;
        }
        
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialsFilePath)
            ).createScoped(SCOPES);
            
            driveService = new Drive.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new HttpCredentialsAdapter(credentials)
            )
            .setApplicationName(applicationName)
            .build();
            
            log.info("Google Drive Service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Google Drive Service: {}", e.getMessage());
            log.warn("Google Drive features will be disabled. Error: {}", e.getMessage());
        }
    }
    
    /**
     * Upload file to Google Drive
     * @param multipartFile The file to upload
     * @param candidateId The candidate ID for organizing files
     * @return Google Drive file ID
     */
    public String uploadFile(MultipartFile multipartFile, Long candidateId) throws IOException {
        log.info("Uploading file {} for candidate {}", multipartFile.getOriginalFilename(), candidateId);
        
        // Create temporary file
        java.io.File tempFile = java.io.File.createTempFile("upload-", multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        
        try {
            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String fileName = String.format("candidate_%d_%s_%s", 
                candidateId, timestamp, multipartFile.getOriginalFilename());
            
            // Create file metadata
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            
            // Set parent folder if specified
            if (folderId != null && !folderId.isEmpty()) {
                fileMetadata.setParents(Collections.singletonList(folderId));
            }
            
            // Upload file
            FileContent mediaContent = new FileContent(
                multipartFile.getContentType(), 
                tempFile
            );
            
            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id, name, mimeType, size, createdTime")
                .execute();
            
            log.info("File uploaded successfully. Drive File ID: {}", uploadedFile.getId());
            return uploadedFile.getId();
            
        } finally {
            // Cleanup temp file
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * Download file from Google Drive
     * @param fileId Google Drive file ID
     * @return InputStream of file content
     */
    public InputStream downloadFile(String fileId) throws IOException {
        log.info("Downloading file with ID: {}", fileId);
        return driveService.files().get(fileId).executeMediaAsInputStream();
    }
    
    /**
     * Delete file from Google Drive
     * @param fileId Google Drive file ID
     */
    public void deleteFile(String fileId) throws IOException {
        log.info("Deleting file with ID: {}", fileId);
        driveService.files().delete(fileId).execute();
        log.info("File deleted successfully");
    }
    
    /**
     * Generate shareable link for file
     * Creates a permission that allows anyone with link to view the file
     * @param fileId Google Drive file ID
     * @return Shareable link URL
     */
    public String generateShareableLink(String fileId) throws IOException {
        log.info("Generating shareable link for file: {}", fileId);
        
        // Create permission for anyone with link
        Permission permission = new Permission()
            .setType("anyone")
            .setRole("reader");
        
        driveService.permissions().create(fileId, permission)
            .setFields("id")
            .execute();
        
        // Get file to retrieve web view link
        File file = driveService.files().get(fileId)
            .setFields("webViewLink, webContentLink")
            .execute();
        
        String shareableLink = file.getWebViewLink();
        log.info("Shareable link generated: {}", shareableLink);
        
        return shareableLink;
    }
    
    /**
     * Get file metadata
     * @param fileId Google Drive file ID
     * @return File metadata
     */
    public File getFileMetadata(String fileId) throws IOException {
        return driveService.files().get(fileId)
            .setFields("id, name, mimeType, size, createdTime, modifiedTime")
            .execute();
    }
    
    /**
     * Check if file exists
     * @param fileId Google Drive file ID
     * @return true if file exists
     */
    public boolean fileExists(String fileId) {
        try {
            driveService.files().get(fileId).setFields("id").execute();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
