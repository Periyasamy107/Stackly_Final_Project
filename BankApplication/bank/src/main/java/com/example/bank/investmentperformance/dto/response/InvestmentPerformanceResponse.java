package com.example.bank.investmentperformance.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentPerformanceResponse {

    private Long id;

    private Long investmentId;

    private Long customerId;

    private Long accountId;

    private LocalDate performanceDate;

    private BigDecimal investedAmount;

    private BigDecimal currentValue;

    private BigDecimal profitLossAmount;

    private BigDecimal returnPercentage;

    private String notes;

    private LocalDateTime createdAt;
}