package com.example.bank.transaction.dto.response;

import com.example.bank.common.enums.TransactionStatus;
import com.example.bank.common.enums.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;

    private Long accountId;

    private String accountNumber;

    private String transactionReference;

    private TransactionType transactionType;

    private BigDecimal amount;

    private LocalDateTime transactionDate;

    private String description;

    private String counterpartyAccount;

    private TransactionStatus transactionStatus;
}