package com.example.bank.loan.entity;

import com.example.bank.account.entity.Account;
import com.example.bank.common.enums.LoanStatus;
import com.example.bank.common.enums.LoanType;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "loans",
        indexes = {
                @Index(
                        name = "idx_loans_customer_id",
                        columnList = "customer_id"
                ),
                @Index(
                        name = "idx_loans_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_loans_start_date",
                        columnList = "start_date"
                ),
                @Index(
                        name = "idx_loans_end_date",
                        columnList = "end_date"
                ),
                @Index(
                        name = "idx_loans_disbursement_account_id",
                        columnList = "disbursement_account_id"
                ),
                @Index(
                        name = "idx_loans_disbursed_at",
                        columnList = "disbursed_at"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_loans_disbursement_transaction_reference",
                        columnNames = "disbursement_transaction_reference"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "loan_type",
            nullable = false,
            length = 30,
            updatable = false
    )
    private LoanType loanType;

    @Column(
            name = "principal_amount",
            nullable = false,
            precision = 19,
            scale = 4,
            updatable = false
    )
    private BigDecimal principalAmount;

    @Column(
            name = "interest_rate",
            precision = 7,
            scale = 4
    )
    private BigDecimal interestRate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private LoanStatus status;

    @Column(
            name = "rejection_reason",
            length = 500
    )
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disbursement_account_id")
    private Account disbursementAccount;

    @Column(
            name = "disbursement_transaction_reference",
            length = 50,
            unique = true
    )
    private String disbursementTransactionReference;

    @Column(
            name = "disbursed_at"
    )
    private LocalDateTime disbursedAt;

    @CreatedDate
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(
            name = "created_by",
            length = 100,
            updatable = false
    )
    private String createdBy;

    @LastModifiedBy
    @Column(
            name = "updated_by",
            length = 100
    )
    private String updatedBy;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = DateTimeUtil.nowUtc();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (status == null) {
            status = LoanStatus.PENDING;
        }

        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = DateTimeUtil.nowUtc();
    }
}