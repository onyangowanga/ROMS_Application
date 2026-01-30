package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.dto.JobOrderSummaryDTO;
import com.roms.entity.Employer;
import com.roms.entity.JobOrder;
import com.roms.entity.User;
import com.roms.enums.JobOrderStatus;
import com.roms.repository.EmployerRepository;
import com.roms.repository.JobOrderRepository;
import com.roms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/job-orders")
public class JobOrderController {

    @Autowired
    private JobOrderRepository jobOrderRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all job orders (accessible to all authenticated users)
     */
    @GetMapping
    public ResponseEntity<?> getAllJobOrders() {
        List<JobOrder> jobOrders = jobOrderRepository.findAllActive();
        List<JobOrderSummaryDTO> summaries = jobOrders.stream()
                .map(JobOrderSummaryDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Job orders retrieved successfully", summaries));
    }

    /**
     * Get job order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getJobOrderById(@PathVariable Long id) {
        JobOrder jobOrder = jobOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job order not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.success("Job order retrieved successfully", jobOrder));
    }

    /**
     * Create new job order (Employer or Super Admin)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPLOYER')")
    public ResponseEntity<?> createJobOrder(@RequestBody JobOrder jobOrder, Authentication authentication) {
        // Validate job order reference is unique
        if (jobOrderRepository.findByJobOrderRef(jobOrder.getJobOrderRef()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Job order reference already exists"));
        }

        // Get the authenticated user
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is admin
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN"));

        // Set employer: Admin should provide employer_id in request, Employer user is looked up by email
        if (isAdmin) {
            // Admin must specify employer in the request, or it should already be set
            if (jobOrder.getEmployer() == null || jobOrder.getEmployer().getId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Employer must be specified when creating a job order"));
            }
            // Verify the employer exists
            Employer employer = employerRepository.findById(jobOrder.getEmployer().getId())
                    .orElseThrow(() -> new RuntimeException("Employer not found with id: " + jobOrder.getEmployer().getId()));
            jobOrder.setEmployer(employer);
        } else {
            // For EMPLOYER role, find their employer record and set it
            Employer employer = employerRepository.findByContactEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("Employer profile not found for this user. Please contact admin."));
            jobOrder.setEmployer(employer);
        }

        // Set initial status: OPEN for admins (auto-approved), PENDING_APPROVAL for employers
        if (isAdmin) {
            jobOrder.setStatus(JobOrderStatus.OPEN);
        } else {
            jobOrder.setStatus(JobOrderStatus.PENDING_APPROVAL);
        }
        jobOrder.setHeadcountFilled(0);

        JobOrder savedJobOrder = jobOrderRepository.save(jobOrder);

        String message = isAdmin
            ? "Job order created successfully and is now open"
            : "Job order created successfully and pending approval";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, savedJobOrder));
    }

    /**
     * Update job order status (Super Admin only for approval/rejection)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateJobOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        JobOrder jobOrder = jobOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job order not found with id: " + id));

        String status = request.get("status");
        try {
            JobOrderStatus newStatus = JobOrderStatus.valueOf(status);
            jobOrder.setStatus(newStatus);
            JobOrder updatedJobOrder = jobOrderRepository.save(jobOrder);
            return ResponseEntity.ok(ApiResponse.success("Job order status updated successfully", updatedJobOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status: " + status));
        }
    }

    /**
     * Update job order details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'EMPLOYER')")
    public ResponseEntity<?> updateJobOrder(@PathVariable Long id, @RequestBody JobOrder jobOrderDetails) {
        JobOrder jobOrder = jobOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job order not found with id: " + id));

        // Update fields
        jobOrder.setJobTitle(jobOrderDetails.getJobTitle());
        jobOrder.setDescription(jobOrderDetails.getDescription());
        jobOrder.setHeadcountRequired(jobOrderDetails.getHeadcountRequired());
        jobOrder.setSalaryMin(jobOrderDetails.getSalaryMin());
        jobOrder.setSalaryMax(jobOrderDetails.getSalaryMax());
        jobOrder.setCurrency(jobOrderDetails.getCurrency());
        jobOrder.setLocation(jobOrderDetails.getLocation());
        jobOrder.setCountry(jobOrderDetails.getCountry());
        jobOrder.setContractDurationMonths(jobOrderDetails.getContractDurationMonths());
        jobOrder.setRequiredSkills(jobOrderDetails.getRequiredSkills());

        JobOrder updatedJobOrder = jobOrderRepository.save(jobOrder);
        return ResponseEntity.ok(ApiResponse.success("Job order updated successfully", updatedJobOrder));
    }

    /**
     * Delete job order (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteJobOrder(@PathVariable Long id) {
        JobOrder jobOrder = jobOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job order not found with id: " + id));

        jobOrder.softDelete();
        jobOrderRepository.save(jobOrder);

        return ResponseEntity.ok(ApiResponse.success("Job order deleted successfully"));
    }
}
