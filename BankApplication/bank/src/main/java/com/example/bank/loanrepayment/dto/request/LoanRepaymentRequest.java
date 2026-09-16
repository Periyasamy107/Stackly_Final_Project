package com.example.bank.loanrepayment.dto.request;

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
public class LoanRepaymentRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Repayment amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Repayment amount must be greater than zero"
    )
    private BigDecimal amount;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;
}