package com.roms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.roms.entity.base.BaseAuditEntity;
import com.roms.enums.JobOrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_orders",
       indexes = {
           @Index(name = "idx_job_order_ref", columnList = "job_order_ref"),
           @Index(name = "idx_status", columnList = "status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class JobOrder extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_order_ref", unique = true, nullable = false, length = 50)
    private String jobOrderRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "jobOrders"})
    private Employer employer;

    @NotBlank
    @Column(name = "job_title", nullable = false, length = 200)
    private String jobTitle;

    @Column(length = 2000)
    private String description;

    @Positive
    @Column(name = "headcount_required", nullable = false)
    private Integer headcountRequired;

    @Column(name = "headcount_filled", nullable = false)
    @Builder.Default
    private Integer headcountFilled = 0;

    @Column(name = "salary_min")
    private BigDecimal salaryMin;

    @Column(name = "salary_max")
    private BigDecimal salaryMax;

    @Column(length = 100)
    private String currency;

    @Column(length = 100)
    private String location;

    @Column(length = 100)
    private String country;

    @Column(name = "contract_duration_months")
    private Integer contractDurationMonths;

    @Column(name = "required_skills", length = 1000)
    private String requiredSkills;

    @Column(name = "required_experience_years")
    private Integer requiredExperienceYears;

    @Column(name = "required_education", length = 200)
    private String requiredEducation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobOrderStatus status = JobOrderStatus.OPEN;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "requires_interview")
    @Builder.Default
    private Boolean requiresInterview = true;

    // Assignments relationship (replaces direct candidates collection)
    @OneToMany(mappedBy = "jobOrder", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("jobOrder")
    @Builder.Default
    private List<Assignment> assignments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (jobOrderRef == null) {
            jobOrderRef = "JO" + System.currentTimeMillis();
        }
        if (headcountFilled == null) {
            headcountFilled = 0;
        }
    }

    /**
     * Check if job order is filled
     */
    public boolean isFilled() {
        return headcountFilled >= headcountRequired;
    }

    /**
     * Get remaining positions
     */
    public int getRemainingPositions() {
        return headcountRequired - headcountFilled;
    }

    /**
     * Increment filled count
     */
    public void incrementFilledCount() {
        if (headcountFilled < headcountRequired) {
            headcountFilled++;
            if (isFilled()) {
                status = JobOrderStatus.FILLED;
            }
        }
    }
}
