package com.example.bank.investment.dto.request;

import com.example.bank.common.enums.InvestmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    @NotNull(message = "Investment type is required")
    private InvestmentType investmentType;

    @NotNull(message = "Investment amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Investment amount must be greater than zero"
    )
    private BigDecimal amount;

    @NotNull(message = "Maturity date is required")
//    @Future(message = "Maturity date must be in the future")
    private LocalDate maturityDate;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;
}