package com.example.bank.loanrepayment.repository;

import com.example.bank.common.enums.LoanRepaymentStatus;
import com.example.bank.loanrepayment.entity.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LoanRepaymentRepository
        extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoanIdOrderByRepaymentDateDesc(
            Long loanId
    );

    List<LoanRepayment> findByAccountIdOrderByRepaymentDateDesc(
            Long accountId
    );

    Optional<LoanRepayment> findByTransactionReference(
            String transactionReference
    );

    boolean existsByTransactionReference(
            String transactionReference
    );

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0)
            FROM LoanRepayment r
            WHERE r.loan.id = :loanId
              AND r.status = com.example.bank.common.enums.LoanRepaymentStatus.COMPLETED
            """)
    BigDecimal sumCompletedRepaymentsByLoanId(
            @Param("loanId") Long loanId
    );
}