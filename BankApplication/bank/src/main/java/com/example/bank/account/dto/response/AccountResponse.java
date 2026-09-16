package com.example.bank.account.dto.response;

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
public class AccountResponse {

    private Long id;

    private Long customerId;

    private String accountNumber;

    private AccountType accountType;

    private BigDecimal balance;

    private AccountStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long version;
}