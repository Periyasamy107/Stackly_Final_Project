package com.example.bank.transaction.repository;

import com.example.bank.common.enums.TransactionStatus;
import com.example.bank.transaction.entity.Transaction;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionReference(
            String transactionReference
    );

    List<Transaction> findByAccountIdOrderByTransactionDateDesc(
            Long accountId
    );

    Page<Transaction> findByAccountId(
            Long accountId,
            Pageable pageable
    );

    List<Transaction> findByTransactionStatus(
            TransactionStatus status
    );

    boolean existsByTransactionReference(
            String transactionReference
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.id = :id
        """)
    Optional<Transaction> findByIdForUpdate(
            @Param("id") Long id
    );
}