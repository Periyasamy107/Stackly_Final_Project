package com.example.bank.loanrepayment.entity;

import com.example.bank.account.entity.Account;
import com.example.bank.common.enums.LoanRepaymentStatus;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.loan.entity.Loan;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "loan_repayments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_loan_repayments_transaction_reference",
                        columnNames = "transaction_reference"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "loan_id",
            nullable = false
    )
    private Loan loan;

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

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 4,
            updatable = false
    )
    private BigDecimal amount;

    @Column(
            name = "repayment_date",
            nullable = false,
            updatable = false
    )
    private LocalDateTime repaymentDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private LoanRepaymentStatus status;

    @Column(
            name = "description",
            length = 500,
            updatable = false
    )
    private String description;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = DateTimeUtil.nowUtc();

        if (repaymentDate == null) {
            repaymentDate = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (status == null) {
            status = LoanRepaymentStatus.COMPLETED;
        }

        if (version == null) {
            version = 0L;
        }
    }
}