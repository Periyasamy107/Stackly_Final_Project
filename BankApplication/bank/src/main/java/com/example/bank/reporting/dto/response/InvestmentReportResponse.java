package com.example.bank.reporting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentReportResponse {

    private Long investmentId;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private Long accountId;

    private String accountNumber;

    private String investmentType;

    private BigDecimal amount;

    private LocalDate startDate;

    private LocalDate maturityDate;

    private String status;

    private BigDecimal currentValue;

    private BigDecimal totalPerformanceAmount;

    private BigDecimal totalPerformancePercentage;

    private long performanceRecordCount;
}