package com.example.bank.investmentperformance.entity;

import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.investment.entity.Investment;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "investment_performance",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_investment_performance_date",
                        columnNames = {
                                "investment_id",
                                "performance_date"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "investment_id",
            nullable = false
    )
    private Investment investment;

    @Column(
            name = "performance_date",
            nullable = false,
            updatable = false
    )
    private LocalDate performanceDate;

    @Column(
            name = "invested_amount",
            nullable = false,
            precision = 19,
            scale = 4,
            updatable = false
    )
    private BigDecimal investedAmount;

    @Column(
            name = "current_value",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal currentValue;

    @Column(
            name = "profit_loss_amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal profitLossAmount;

    @Column(
            name = "return_percentage",
            nullable = false,
            precision = 19,
            scale = 8
    )
    private BigDecimal returnPercentage;

    @Column(
            name = "notes",
            length = 500
    )
    private String notes;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = DateTimeUtil.nowUtc();
        }
    }
}