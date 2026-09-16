package com.example.bank.loan.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApprovalRequest {

    @NotNull(message = "Interest rate is required")
    @DecimalMin(
            value = "0.0000",
            message = "Interest rate cannot be negative"
    )
    private BigDecimal interestRate;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
}