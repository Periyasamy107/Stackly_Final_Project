package com.example.bank.loan.dto.response;

import com.example.bank.common.enums.LoanStatus;
import com.example.bank.common.enums.LoanType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponse {

    private Long id;

    private Long customerId;

    private LoanType loanType;

    private BigDecimal principalAmount;

    private BigDecimal interestRate;

    private LocalDate startDate;

    private LocalDate endDate;

    private LoanStatus status;

    private String rejectionReason;

    private Long disbursementAccountId;

    private String disbursementTransactionReference;

    private LocalDateTime disbursedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long version;
}