package com.example.bank.loan.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDisbursementRequest {

    @NotNull(message = "Disbursement account id is required")
    private Long accountId;
}