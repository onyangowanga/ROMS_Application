package com.roms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.roms.entity.base.BaseAuditEntity;
import com.roms.enums.CandidateStatus;
import com.roms.enums.MedicalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates", 
       indexes = {
           @Index(name = "idx_internal_ref_no", columnList = "internal_ref_no"),
           @Index(name = "idx_passport_no", columnList = "passport_no"),
           @Index(name = "idx_current_status", columnList = "current_status")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Candidate extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "internal_ref_no", unique = true, nullable = false, length = 50)
    private String internalRefNo;

    @NotBlank
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @NotBlank
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Past
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 10)
    private String gender;

    @NotBlank
    @Column(name = "passport_no", length = 50)
    private String passportNo;

    @Column(name = "passport_expiry")
    private LocalDate passportExpiry;

    @Column(name = "national_id", length = 50)
    private String nationalId;

    @Email
    @Column(nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "alternate_phone", length = 20)
    private String alternatePhone;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    @Builder.Default
    private CandidateStatus currentStatus = CandidateStatus.APPLIED;

    @Enumerated(EnumType.STRING)
    @Column(name = "medical_status")
    @Builder.Default
    private MedicalStatus medicalStatus = MedicalStatus.PENDING;

    @Column(name = "medical_test_date")
    private LocalDate medicalTestDate;

    @Column(name = "medical_expiry")
    private LocalDate medicalExpiry;

    @Column(name = "expiry_flag", length = 20)
    private String expiryFlag; // EXPIRING_SOON, EXPIRED, VALID

    // Interview scheduling fields
    @Column(name = "interview_date")
    private LocalDate interviewDate;

    @Column(name = "interview_time", length = 10)
    private String interviewTime;

    @Column(name = "interview_location", length = 500)
    private String interviewLocation;

    @Column(name = "interview_notes", length = 1000)
    private String interviewNotes;

    // Assignment relationship (replaces direct jobOrder)
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("candidate")
    @Builder.Default
    private List<Assignment> assignments = new ArrayList<>();

    @Column(name = "expected_position", length = 200)
    private String expectedPosition;

    @Column(name = "expected_salary")
    private java.math.BigDecimal expectedSalary;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(length = 200)
    private String education;

    @Column(length = 1000)
    private String skills;

    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("candidate")
    @Builder.Default
    private List<CandidateDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("candidate")
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (internalRefNo == null) {
            // Generate reference number - can be customized
            internalRefNo = "CND" + System.currentTimeMillis();
        }
    }

    /**
     * Get full name
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        fullName.append(" ").append(lastName);
        return fullName.toString();
    }
}
