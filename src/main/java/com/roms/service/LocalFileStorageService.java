package com.roms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local file storage service for development/testing
 * Stores files in the local filesystem instead of Google Drive
 */
@Service
@Slf4j
public class LocalFileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            log.info("Local file storage initialized at: {}", uploadPath);
        } catch (IOException ex) {
            log.error("Could not create upload directory!", ex);
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    /**
     * Store file locally and return a unique file identifier
     */
    public String storeFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        try {
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("File stored successfully: {}", fileName);
            return fileName;
        } catch (IOException ex) {
            log.error("Could not store file {}. Error: {}", fileName, ex.getMessage());
            throw new IOException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * Load file as Path
     */
    public Path loadFile(String fileName) {
        return uploadPath.resolve(fileName).normalize();
    }

    /**
     * Delete file
     */
    public void deleteFile(String fileName) throws IOException {
        Path filePath = loadFile(fileName);
        Files.deleteIfExists(filePath);
        log.info("File deleted: {}", fileName);
    }

    /**
     * Generate a shareable URL (local path for now)
     */
    public String generateShareableLink(String fileName) {
        return "/api/files/" + fileName;
    }
}
