package com.roms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.roms.entity.base.BaseAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
public class Employer extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "company_registration_no", unique = true, length = 100)
    private String companyRegistrationNo;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String industry;

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("employer")
    @Builder.Default
    private List<JobOrder> jobOrders = new ArrayList<>();
}
