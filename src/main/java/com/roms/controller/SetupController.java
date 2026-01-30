package com.roms.controller;

import com.roms.entity.User;
import com.roms.enums.UserRole;
import com.roms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/init-admin")
    public ResponseEntity<?> initializeAdminGet() {
        return initializeAdmin();
    }

    @PostMapping("/init-admin")
    public ResponseEntity<?> initializeAdmin() {
        // Check if admin user already exists
        if (userRepository.findByUsername("admin").isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Admin user already exists");
            response.put("status", "skipped");
            return ResponseEntity.ok(response);
        }

        // Create default admin user
        User admin = User.builder()
                .username("admin")
                .email("admin@roms.com")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.SUPER_ADMIN)
                .fullName("System Administrator")
                .isEmailVerified(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .build();

        userRepository.save(admin);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Admin user created successfully");
        response.put("username", "admin");
        response.put("password", "admin123");
        response.put("status", "created");

        return ResponseEntity.ok(response);
    }
}
