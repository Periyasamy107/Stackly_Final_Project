package com.example.bank.reporting.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportResponse {

    private Long transactionId;

    private Long accountId;

    private String accountNumber;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private String transactionType;

    private BigDecimal amount;

    private LocalDateTime transactionDate;

    private String description;

    private String transactionReference;

    private String counterpartyAccount;

    private String transactionStatus;
}