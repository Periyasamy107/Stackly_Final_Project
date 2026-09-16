package com.example.bank.account.repository;

import com.example.bank.account.entity.Account;
import com.example.bank.common.enums.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository
        extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(
            String accountNumber
    );

    List<Account> findByCustomerId(
            Long customerId
    );

    List<Account> findByCustomerIdAndStatus(
            Long customerId,
            AccountStatus status
    );

    boolean existsByAccountNumber(
            String accountNumber
    );

    boolean existsByCustomerId(
            Long customerId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.id = :id
            """)
    Optional<Account> findByIdForUpdate(
            @Param("id") Long id
    );

    @Query("""
    select case when count(a) > 0 then true else false end
    from Account a
    join a.customer c
    join c.user u
    where a.id = :accountId
      and u.id = :userId
""")
    boolean existsByIdAndCustomerUserId(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId
    );
}