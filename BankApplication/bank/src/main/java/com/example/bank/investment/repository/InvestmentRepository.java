package com.example.bank.investment.repository;

import com.example.bank.common.enums.InvestmentStatus;
import com.example.bank.investment.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvestmentRepository
        extends JpaRepository<Investment, Long> {

    List<Investment> findByCustomerIdOrderByCreatedAtDesc(
            Long customerId
    );

    List<Investment> findByAccountIdOrderByCreatedAtDesc(
            Long accountId
    );

    List<Investment> findByStatus(
            InvestmentStatus status
    );

    List<Investment> findByStatusAndMaturityDateLessThanEqual(
            InvestmentStatus status,
            LocalDate maturityDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM Investment i
            WHERE i.id = :id
            """)
    Optional<Investment> findByIdForUpdate(
            @Param("id") Long id
    );
}