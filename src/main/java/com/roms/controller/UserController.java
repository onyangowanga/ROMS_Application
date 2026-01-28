package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.dto.RegisterRequest;
import com.roms.entity.User;
import com.roms.enums.UserRole;
import com.roms.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Management Controller
 * Handles user CRUD operations - restricted to SUPER_ADMIN only
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    /**
     * Create a new user (Admin only)
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Username is already taken!"));
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Email is already in use!"));
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole(registerRequest.getRole());
        user.setIsActive(true);
        user.setIsLocked(false);
        user.setIsEmailVerified(true); // Admin-created users are auto-verified

        User savedUser = userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully", savedUser));
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    /**
     * Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody RegisterRequest updateRequest) {
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if new username is taken by another user
        if (!user.getUsername().equals(updateRequest.getUsername()) &&
            userRepository.existsByUsername(updateRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Username is already taken!"));
        }

        // Check if new email is taken by another user
        if (!user.getEmail().equals(updateRequest.getEmail()) &&
            userRepository.existsByEmail(updateRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Email is already in use!"));
        }

        // Update user fields
        user.setUsername(updateRequest.getUsername());
        user.setEmail(updateRequest.getEmail());
        user.setFullName(updateRequest.getFullName());
        user.setRole(updateRequest.getRole());
        
        // Only update password if provided
        if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
    }

    /**
     * Delete user (soft delete by deactivating)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Prevent deleting super admin
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Cannot delete super admin user!"));
        }

        // Soft delete by deactivating
        user.setIsActive(false);
        userRepository.save(user);
        
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully"));
    }

    /**
     * Activate/Deactivate user
     */
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleUserActive(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);
        
        String message = updatedUser.getIsActive() ? "User activated" : "User deactivated";
        return ResponseEntity.ok(ApiResponse.success(message, updatedUser));
    }
}
