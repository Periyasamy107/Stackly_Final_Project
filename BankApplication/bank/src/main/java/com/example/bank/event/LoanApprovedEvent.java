package com.example.bank.event;

import com.example.bank.common.enums.LoanType;

import java.math.BigDecimal;
import java.util.UUID;

public record LoanApprovedEvent(
        String eventId,
        Long userId,
        Long loanId,
        LoanType loanType,
        BigDecimal principalAmount
) implements DomainEvent {

    public LoanApprovedEvent(
            Long userId,
            Long loanId,
            LoanType loanType,
            BigDecimal principalAmount) {

        this(
                UUID.randomUUID().toString(),
                userId,
                loanId,
                loanType,
                principalAmount
        );
    }
}