package com.roms.controller;

import com.roms.dto.ApiResponse;
import com.roms.entity.Candidate;
import com.roms.entity.Employer;
import com.roms.entity.JobOrder;
import com.roms.enums.CandidateStatus;
import com.roms.enums.JobOrderStatus;
import com.roms.repository.CandidateRepository;
import com.roms.repository.EmployerRepository;
import com.roms.repository.JobOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private JobOrderRepository jobOrderRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'OPERATIONS_STAFF', 'FINANCE_MANAGER')")
    public ResponseEntity<?> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total candidates
        long totalCandidates = candidateRepository.findAllActive().size();
        stats.put("totalCandidates", totalCandidates);
        
        // Total employers
        long totalEmployers = employerRepository.findAllActive().size();
        stats.put("totalEmployers", totalEmployers);
        
        // Total job orders
        long totalJobOrders = jobOrderRepository.findAllActive().size();
        stats.put("totalJobOrders", totalJobOrders);
        
        // Open positions (sum of headcount required for open job orders)
        int openPositions = jobOrderRepository.findByStatus(JobOrderStatus.OPEN)
                .stream()
                .mapToInt(job -> job.getHeadcountRequired())
                .sum();
        stats.put("openPositions", openPositions);
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats retrieved successfully", stats));
    }

    @GetMapping("/employer/stats")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<?> getEmployerDashboardStats(@RequestParam String email) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Debug: Log the email being searched
            System.out.println("Searching for employer with email: " + email);
            
            // Find employer by contact email
            Employer employer = employerRepository.findByContactEmail(email)
                    .orElse(null);
            
            System.out.println("Employer found: " + (employer != null ? employer.getCompanyName() : "NULL"));
            
            if (employer == null) {
                System.out.println("No employer found with email: " + email);
                // If no employer found, return empty stats
                stats.put("totalJobsPosted", 0);
                stats.put("totalHeadcountRequired", 0);
                stats.put("pendingPositionsHeadcount", 0);
                stats.put("totalApplicationsReceived", 0);
                stats.put("totalPlaced", 0);
                stats.put("filledPositions", 0);
                return ResponseEntity.ok(ApiResponse.success("No employer profile found. Please contact admin to link your account.", stats));
            }
            
            // Get all job orders for this employer
            List<JobOrder> allJobs = jobOrderRepository.findByEmployerId(employer.getId());
            System.out.println("Jobs found for employer: " + allJobs.size());
            
            // Total jobs posted by this employer
            stats.put("totalJobsPosted", allJobs.size());
            
            // Total headcount required (sum of headcount for all jobs)
            int totalHeadcount = allJobs.stream()
                    .mapToInt(JobOrder::getHeadcountRequired)
                    .sum();
            stats.put("totalHeadcountRequired", totalHeadcount);
            
            // Pending positions headcount (sum of unfilled positions for OPEN jobs)
            int pendingPositions = allJobs.stream()
                    .filter(job -> job.getStatus() == JobOrderStatus.OPEN)
                    .mapToInt(job -> job.getHeadcountRequired() - job.getHeadcountFilled())
                    .sum();
            stats.put("pendingPositionsHeadcount", pendingPositions);
            
            // Total applications received (count of candidates across all job orders)
            int totalApplications = 0;
            for (JobOrder job : allJobs) {
                totalApplications += candidateRepository.findByJobOrderId(job.getId()).size();
            }
            stats.put("totalApplicationsReceived", totalApplications);
            
            // Total placed candidates
            int totalPlaced = 0;
            for (JobOrder job : allJobs) {
                List<Candidate> candidates = candidateRepository.findByJobOrderId(job.getId());
                totalPlaced += (int) candidates.stream()
                        .filter(c -> c.getCurrentStatus() == CandidateStatus.PLACED)
                        .count();
            }
            stats.put("totalPlaced", totalPlaced);
            
            // Filled positions (total headcount filled)
            int filledPositions = allJobs.stream()
                    .mapToInt(JobOrder::getHeadcountFilled)
                    .sum();
            stats.put("filledPositions", filledPositions);
            
            System.out.println("Stats calculated: " + stats);
            
            return ResponseEntity.ok(ApiResponse.success("Employer dashboard stats retrieved successfully", stats));
        } catch (Exception e) {
            e.printStackTrace();
            // Return error with empty stats
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalJobsPosted", 0);
            emptyStats.put("totalHeadcountRequired", 0);
            emptyStats.put("pendingPositionsHeadcount", 0);
            emptyStats.put("totalApplicationsReceived", 0);
            emptyStats.put("totalPlaced", 0);
            emptyStats.put("filledPositions", 0);
            return ResponseEntity.ok(ApiResponse.success("Error loading stats: " + e.getMessage(), emptyStats));
        }
    }
}
