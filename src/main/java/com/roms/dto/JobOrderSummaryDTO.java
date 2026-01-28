package com.roms.dto;

import com.roms.entity.JobOrder;
import com.roms.enums.JobOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderSummaryDTO {
    private Long id;
    private String jobOrderRef;
    private String jobTitle;
    private String description;
    private Integer headcountRequired;
    private Integer headcountFilled;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String location;
    private String country;
    private Integer contractDurationMonths;
    private String requiredSkills;
    private JobOrderStatus status;
    
    // Static factory method to create from JobOrder entity
    public static JobOrderSummaryDTO from(JobOrder jobOrder) {
        return JobOrderSummaryDTO.builder()
                .id(jobOrder.getId())
                .jobOrderRef(jobOrder.getJobOrderRef())
                .jobTitle(jobOrder.getJobTitle())
                .description(jobOrder.getDescription())
                .headcountRequired(jobOrder.getHeadcountRequired())
                .headcountFilled(jobOrder.getHeadcountFilled())
                .salaryMin(jobOrder.getSalaryMin())
                .salaryMax(jobOrder.getSalaryMax())
                .currency(jobOrder.getCurrency())
                .location(jobOrder.getLocation())
                .country(jobOrder.getCountry())
                .contractDurationMonths(jobOrder.getContractDurationMonths())
                .requiredSkills(jobOrder.getRequiredSkills())
                .status(jobOrder.getStatus())
                .build();
    }
}
