package com.example.bank.transaction.entity;

import com.example.bank.account.entity.Account;
import com.example.bank.common.enums.TransactionStatus;
import com.example.bank.common.enums.TransactionType;
import com.example.bank.common.util.DateTimeUtil;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transactions_reference",
                        columnNames = "transaction_reference"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false
    )
    private Account account;

    @Column(
            name = "transaction_reference",
            nullable = false,
            unique = true,
            length = 50,
            updatable = false
    )
    private String transactionReference;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "transaction_type",
            nullable = false,
            length = 30,
            updatable = false
    )
    private TransactionType transactionType;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 4,
            updatable = false
    )
    private BigDecimal amount;

    @Column(
            name = "transaction_date",
            nullable = false,
            updatable = false
    )
    private LocalDateTime transactionDate;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(
            name = "counterparty_account",
            length = 30,
            updatable = false
    )
    private String counterpartyAccount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "transaction_status",
            nullable = false,
            length = 30
    )
    private TransactionStatus transactionStatus;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = DateTimeUtil.nowUtc();

        if (transactionDate == null) {
            transactionDate = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (transactionStatus == null) {
            transactionStatus =
                    TransactionStatus.COMPLETED;
        }
    }
}