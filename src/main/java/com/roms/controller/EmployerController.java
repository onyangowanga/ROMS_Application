package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.dto.EmployerRegistrationRequest;
import com.roms.entity.Employer;
import com.roms.entity.User;
import com.roms.enums.UserRole;
import com.roms.repository.EmployerRepository;
import com.roms.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employers")
public class EmployerController {

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Public endpoint for employer registration
     * Creates both user account and employer record
     */
    @PostMapping
    @Transactional
    public ResponseEntity<?> registerEmployer(@Valid @RequestBody EmployerRegistrationRequest request) {
        try {
            // Validate username doesn't exist
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username is already taken"));
            }

            // Validate email doesn't exist
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already registered"));
            }

            // Create user account with EMPLOYER role
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getContactPerson())
                    .phoneNumber(request.getContactPhone())
                    .role(UserRole.EMPLOYER)
                    .isEmailVerified(false)
                    .build();

            userRepository.save(user);

            // Create employer record
            Employer employer = Employer.builder()
                    .companyName(request.getCompanyName())
                    .contactPerson(request.getContactPerson())
                    .contactEmail(request.getContactEmail())
                    .contactPhone(request.getContactPhone())
                    .address(request.getAddress())
                    .country(request.getCountry())
                    .industry(request.getIndustry())
                    .build();

            Employer savedEmployer = employerRepository.save(employer);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Employer registered successfully! You can now login.", savedEmployer));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Get all employers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF')")
    public ResponseEntity<?> getAllEmployers() {
        List<Employer> employers = employerRepository.findAllActive();
        return ResponseEntity.ok(ApiResponse.success("Employers retrieved successfully", employers));
    }

    /**
     * Get employer by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'EMPLOYER')")
    public ResponseEntity<?> getEmployerById(@PathVariable Long id) {
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employer not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.success("Employer retrieved successfully", employer));
    }

    /**
     * Update employer
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPLOYER')")
    public ResponseEntity<?> updateEmployer(@PathVariable Long id, @RequestBody Employer employerDetails) {
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employer not found with id: " + id));

        // Update fields
        employer.setCompanyName(employerDetails.getCompanyName());
        employer.setContactPerson(employerDetails.getContactPerson());
        employer.setContactEmail(employerDetails.getContactEmail());
        employer.setContactPhone(employerDetails.getContactPhone());
        employer.setAddress(employerDetails.getAddress());
        employer.setCountry(employerDetails.getCountry());
        employer.setIndustry(employerDetails.getIndustry());

        Employer updatedEmployer = employerRepository.save(employer);
        return ResponseEntity.ok(ApiResponse.success("Employer updated successfully", updatedEmployer));
    }

    /**
     * Delete employer (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteEmployer(@PathVariable Long id) {
        Employer employer = employerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employer not found with id: " + id));

        employer.softDelete();
        employerRepository.save(employer);

        return ResponseEntity.ok(ApiResponse.success("Employer deleted successfully"));
    }
}
