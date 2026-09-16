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
public class LoanReportResponse {

    private Long loanId;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private String loanType;

    private BigDecimal principalAmount;

    private BigDecimal interestRate;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private BigDecimal totalRepaidAmount;

    private BigDecimal outstandingAmount;

    private long repaymentCount;

    private BigDecimal completedRepaymentAmount;
}