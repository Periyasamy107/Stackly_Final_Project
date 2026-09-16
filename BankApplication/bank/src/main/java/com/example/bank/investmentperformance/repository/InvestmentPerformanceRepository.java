package com.example.bank.investmentperformance.repository;

import com.example.bank.investmentperformance.entity.InvestmentPerformance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvestmentPerformanceRepository
        extends JpaRepository<InvestmentPerformance, Long> {

    List<InvestmentPerformance>
    findByInvestmentIdOrderByPerformanceDateDesc(
            Long investmentId
    );

    Optional<InvestmentPerformance>
    findByInvestmentIdAndPerformanceDate(
            Long investmentId,
            LocalDate performanceDate
    );

    Optional<InvestmentPerformance>
    findFirstByInvestmentIdOrderByPerformanceDateDesc(
            Long investmentId
    );

    boolean existsByInvestmentIdAndPerformanceDate(
            Long investmentId,
            LocalDate performanceDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM InvestmentPerformance p
            WHERE p.id = :id
            """)
    Optional<InvestmentPerformance> findByIdForUpdate(
            @Param("id") Long id
    );
}