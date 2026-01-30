package com.roms.controller;

import com.roms.entity.User;
import com.roms.enums.UserRole;
import com.roms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Setup controller for initial system configuration
 * Creates default admin user if none exists
 */
@RestController
@RequestMapping("/api/setup")
public class SetupController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/init-admin")
    public ResponseEntity<?> initializeAdmin() {
        // Check if any admin already exists
        if (userRepository.findByRole(UserRole.SUPER_ADMIN).stream().findAny().isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Admin user already exists");
            response.put("status", "skipped");
            return ResponseEntity.ok(response);
        }

        // Create default admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@roms.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(UserRole.SUPER_ADMIN);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userRepository.save(admin);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Admin user created successfully");
        response.put("username", "admin");
        response.put("password", "admin123");
        response.put("status", "created");

        return ResponseEntity.ok(response);
    }
}
