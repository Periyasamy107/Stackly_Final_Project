package com.example.bank.reporting.dto.response;

import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.AccountType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountReportResponse {

    private Long accountId;

    private String accountNumber;

    private AccountType accountType;

    private AccountStatus status;

    private BigDecimal balance;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long transactionCount;

    private BigDecimal totalDeposits;

    private BigDecimal totalWithdrawals;

    private BigDecimal totalTransferDebits;

    private BigDecimal totalTransferCredits;

    private BigDecimal totalTransferAmount;
}