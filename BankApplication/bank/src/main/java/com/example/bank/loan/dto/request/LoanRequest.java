package com.example.bank.loan.dto.request;

import com.example.bank.common.enums.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Principal amount must be greater than zero"
    )
    private BigDecimal principalAmount;
}