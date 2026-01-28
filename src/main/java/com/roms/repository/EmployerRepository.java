package com.roms.repository;

import com.roms.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {
    
    Optional<Employer> findByCompanyRegistrationNo(String companyRegistrationNo);
    
    Optional<Employer> findByCompanyName(String companyName);
    
    Optional<Employer> findByContactEmail(String contactEmail);
    
    @Query("SELECT e FROM Employer e WHERE e.deletedAt IS NULL")
    List<Employer> findAllActive();
}
