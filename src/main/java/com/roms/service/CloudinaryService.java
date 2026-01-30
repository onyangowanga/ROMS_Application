package com.roms.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Cloudinary file storage service for production
 * Stores files in Cloudinary cloud storage
 * Enabled when roms.storage.mode=cloud
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "roms.storage.mode", havingValue = "cloud")
public class CloudinaryService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret
        ));
        log.info("Cloudinary service initialized successfully for cloud: {}", cloudName);
    }

    /**
     * Upload file to Cloudinary
     * @param file The file to upload
     * @param candidateId The candidate ID for organizing files
     * @return Cloudinary public ID
     */
    public String uploadFile(MultipartFile file, Long candidateId) throws IOException {
        log.info("Uploading file {} for candidate {} to Cloudinary", file.getOriginalFilename(), candidateId);

        try {
            // Generate unique filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String publicId = String.format("roms/candidate_%d/%s_%s",
                candidateId, timestamp, file.getOriginalFilename().replaceAll("[^a-zA-Z0-9._-]", "_"));

            // Upload file to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "auto",
                "folder", "roms/documents"
            ));

            String fileId = (String) uploadResult.get("public_id");
            log.info("File uploaded successfully to Cloudinary. Public ID: {}", fileId);
            return fileId;

        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Generate shareable URL for file
     * @param publicId Cloudinary public ID
     * @return Secure URL to access the file
     */
    public String generateShareableLink(String publicId) {
        String url = cloudinary.url()
            .secure(true)
            .generate(publicId);
        log.info("Generated shareable link for {}: {}", publicId, url);
        return url;
    }

    /**
     * Delete file from Cloudinary
     * @param publicId Cloudinary public ID
     */
    public void deleteFile(String publicId) throws IOException {
        try {
            log.info("Deleting file from Cloudinary: {}", publicId);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted successfully from Cloudinary");
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", e.getMessage());
            throw new IOException("Failed to delete file from Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Check if file exists in Cloudinary
     * @param publicId Cloudinary public ID
     * @return true if file exists
     */
    public boolean fileExists(String publicId) {
        try {
            Map result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            return result != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get file URL
     * @param publicId Cloudinary public ID
     * @return URL to access the file
     */
    public String getFileUrl(String publicId) {
        return cloudinary.url().secure(true).generate(publicId);
    }
}
