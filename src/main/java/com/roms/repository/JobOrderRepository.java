package com.roms.repository;

import com.roms.entity.JobOrder;
import com.roms.enums.JobOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobOrderRepository extends JpaRepository<JobOrder, Long> {
    
    Optional<JobOrder> findByJobOrderRef(String jobOrderRef);
    
    List<JobOrder> findByStatus(JobOrderStatus status);
    
    @Query("SELECT j FROM JobOrder j WHERE j.employer.id = :employerId AND j.deletedAt IS NULL")
    List<JobOrder> findByEmployerId(@Param("employerId") Long employerId);
    
    @Query("SELECT j FROM JobOrder j WHERE j.status = :status AND j.deletedAt IS NULL")
    List<JobOrder> findActiveByStatus(@Param("status") JobOrderStatus status);
    
    @Query("SELECT j FROM JobOrder j WHERE j.deletedAt IS NULL")
    List<JobOrder> findAllActive();
}
