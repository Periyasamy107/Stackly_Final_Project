package com.example.bank.loan.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEligibilityResponse {

    private Long customerId;

    private boolean eligible;

    private List<String> reasons;
}