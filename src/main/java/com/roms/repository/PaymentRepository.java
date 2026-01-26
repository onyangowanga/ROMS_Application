package com.roms.repository;

import com.roms.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByTransactionRef(String transactionRef);
    
    @Query("SELECT p FROM Payment p WHERE p.candidate.id = :candidateId ORDER BY p.paymentDate DESC")
    List<Payment> findByCandidateId(@Param("candidateId") Long candidateId);
    
    @Query("SELECT COALESCE(SUM(CASE WHEN p.type = 'DEBIT' THEN p.amount ELSE -p.amount END), 0) " +
           "FROM Payment p WHERE p.candidate.id = :candidateId AND p.isReversal = false")
    BigDecimal calculateBalanceByCandidateId(@Param("candidateId") Long candidateId);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT p FROM Payment p WHERE p.mpesaRef = :mpesaRef")
    Optional<Payment> findByMpesaRef(@Param("mpesaRef") String mpesaRef);
}
