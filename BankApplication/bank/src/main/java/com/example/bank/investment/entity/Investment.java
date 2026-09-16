package com.example.bank.investment.entity;

import com.example.bank.account.entity.Account;
import com.example.bank.common.enums.InvestmentStatus;
import com.example.bank.common.enums.InvestmentType;
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
@Table(name = "investments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false
    )
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "investment_type",
            nullable = false,
            length = 30,
            updatable = false
    )
    private InvestmentType investmentType;

    @Column(
            name = "amount",
            nullable = false,
            precision = 19,
            scale = 4,
            updatable = false
    )
    private BigDecimal amount;

    @Column(
            name = "start_date",
            nullable = false,
            updatable = false
    )
    private LocalDate startDate;

    @Column(
            name = "maturity_date",
            nullable = false
    )
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private InvestmentStatus status;

    @Column(
            name = "description",
            length = 500,
            updatable = false
    )
    private String description;

    @Column(
            name = "settlement_reference",
            length = 100,
            unique = true
    )
    private String settlementReference;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_account_id")
    private Account settlementAccount;

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
            status = InvestmentStatus.ACTIVE;
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