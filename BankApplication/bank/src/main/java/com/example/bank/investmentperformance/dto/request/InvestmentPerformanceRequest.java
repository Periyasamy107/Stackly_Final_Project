package com.example.bank.investmentperformance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentPerformanceRequest {

    @NotNull(message = "Investment ID is required")
    private Long investmentId;

    @NotNull(message = "Performance date is required")
    @PastOrPresent(
            message = "Performance date cannot be in the future"
    )
    private LocalDate performanceDate;

    @NotNull(message = "Current value is required")
    @DecimalMin(
            value = "0.01",
            message = "Current value must be greater than zero"
    )
    private BigDecimal currentValue;

    @Size(
            max = 500,
            message = "Notes cannot exceed 500 characters"
    )
    private String notes;
}