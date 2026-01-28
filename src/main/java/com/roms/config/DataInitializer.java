package com.roms.config;

import com.roms.entity.Employer;
import com.roms.entity.User;
import com.roms.enums.UserRole;
import com.roms.repository.EmployerRepository;
import com.roms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data Initializer - Seeds database with initial super admin user
 * Creates only the super admin on first startup
 * Super admin can then create other users with different roles via the system
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Checking for initial super admin user...");

        // Create super admin user if doesn't exist
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
            log.info("✅ Created super admin user: admin/password123");
            log.info("💡 Super admin can now create users with other roles via the system");
        } else {
            log.info("✅ Super admin user already exists");
        }

        // Create test employer if doesn't exist
        if (employerRepository.findByCompanyName("Test Company Ltd").isEmpty()) {
            Employer employer = new Employer();
            employer.setCompanyName("Test Company Ltd");
            employer.setContactPerson("John Doe");
            employer.setContactEmail("employer@testcompany.com");
            employer.setContactPhone("+1234567890");
            employer.setAddress("123 Business Street, Suite 100");
            employer.setCountry("United States");
            employer.setIndustry("Technology");
            employerRepository.save(employer);
            log.info("✅ Created test employer: Test Company Ltd");
        }
    }
}
