package com.example.bank.loan.repository;

import com.example.bank.common.enums.LoanStatus;
import com.example.bank.loan.entity.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LoanRepository
        extends JpaRepository<Loan, Long> {

    List<Loan> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );

    List<Loan> findByStatus(
            LoanStatus status
    );

    List<Loan> findByCustomerIdAndStatus(
            Long customerId,
            LoanStatus status
    );

    boolean existsByCustomerIdAndStatusIn(
            Long customerId,
            List<LoanStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT l
            FROM Loan l
            WHERE l.id = :id
            """)
    Optional<Loan> findByIdForUpdate(
            @Param("id") Long id
    );
}