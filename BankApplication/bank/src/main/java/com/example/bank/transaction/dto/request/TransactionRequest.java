package com.example.bank.transaction.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    private Long id;

    @NotNull(message = "Account id is required")
    private Long accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.0001",
            message = "Amount must be greater than zero"
    )
    private BigDecimal amount;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;

    private String counterpartyAccount;

    private String transactionReference;
}