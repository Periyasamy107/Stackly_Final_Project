package com.example.bank.account.entity;

import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.AccountType;
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
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_accounts_account_number",
                        columnNames = "account_number"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private Customer customer;

    @Column(
            name = "account_number",
            nullable = false,
            unique = true,
            length = 30,
            updatable = false
    )
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "account_type",
            nullable = false,
            length = 30
    )
    private AccountType accountType;

    @Column(
            name = "balance",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private AccountStatus status;

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
            updatedAt = DateTimeUtil.nowUtc();;
        }

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }

        if (status == null) {
            status = AccountStatus.ACTIVE;
        }

        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}