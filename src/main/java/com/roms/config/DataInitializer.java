package com.roms.config;

import com.roms.entity.User;
import com.roms.enums.UserRole;
import com.roms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer - Seeds database with demo users
 * Creates default admin and test users for Phase 1B frontend testing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking for demo users...");

        // Create demo admin user if doesn't exist
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setEmail("admin@roms.com");
            admin.setFullName("System Administrator");
            admin.setRole(UserRole.SUPER_ADMIN);
            admin.setIsActive(true);
            admin.setIsLocked(false);
            admin.setIsEmailVerified(true);
            userRepository.save(admin);
            log.info("Created demo admin user: admin/password123");
        }

        // Create operations staff user if doesn't exist
        if (!userRepository.existsByUsername("operations")) {
            User operations = new User();
            operations.setUsername("operations");
            operations.setPassword(passwordEncoder.encode("password123"));
            operations.setEmail("operations@roms.com");
            operations.setFullName("Operations Manager");
            operations.setRole(UserRole.OPERATIONS_STAFF);
            operations.setIsActive(true);
            operations.setIsLocked(false);
            operations.setIsEmailVerified(true);
            userRepository.save(operations);
            log.info("Created demo operations user: operations/password123");
        }

        // Create finance manager user if doesn't exist
        if (!userRepository.existsByUsername("finance")) {
            User finance = new User();
            finance.setUsername("finance");
            finance.setPassword(passwordEncoder.encode("password123"));
            finance.setEmail("finance@roms.com");
            finance.setFullName("Finance Manager");
            finance.setRole(UserRole.FINANCE_MANAGER);
            finance.setIsActive(true);
            finance.setIsLocked(false);
            finance.setIsEmailVerified(true);
            userRepository.save(finance);
            log.info("Created demo finance user: finance/password123");
        }

        // Create employer user if doesn't exist
        if (!userRepository.existsByUsername("employer")) {
            User employer = new User();
            employer.setUsername("employer");
            employer.setPassword(passwordEncoder.encode("password123"));
            employer.setEmail("employer@roms.com");
            employer.setFullName("Test Employer");
            employer.setRole(UserRole.EMPLOYER);
            employer.setIsActive(true);
            employer.setIsLocked(false);
            employer.setIsEmailVerified(true);
            userRepository.save(employer);
            log.info("Created demo employer user: employer/password123");
        }

        // Create applicant user if doesn't exist
        if (!userRepository.existsByUsername("applicant")) {
            User applicant = new User();
            applicant.setUsername("applicant");
            applicant.setPassword(passwordEncoder.encode("password123"));
            applicant.setEmail("applicant@roms.com");
            applicant.setFullName("Test Applicant");
            applicant.setRole(UserRole.APPLICANT);
            applicant.setIsActive(true);
            applicant.setIsLocked(false);
            applicant.setIsEmailVerified(true);
            userRepository.save(applicant);
            log.info("Created demo applicant user: applicant/password123");
        }

        log.info("Demo users initialization complete!");
        log.info("Available test users:");
        log.info("  - admin/password123 (SUPER_ADMIN) - Full access");
        log.info("  - operations/password123 (OPERATIONS_STAFF) - Candidates & documents");
        log.info("  - finance/password123 (FINANCE_MANAGER) - View candidates");
        log.info("  - employer/password123 (EMPLOYER) - Limited access");
        log.info("  - applicant/password123 (APPLICANT) - Limited access");
    }
}
