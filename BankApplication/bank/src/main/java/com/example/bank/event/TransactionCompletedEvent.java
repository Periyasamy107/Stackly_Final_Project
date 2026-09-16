package com.example.bank.event;

import com.example.bank.common.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionCompletedEvent(
        String eventId,
        Long userId,
        Long transactionId,
        String transactionReference,
        TransactionType transactionType,
        BigDecimal amount,
        String description
) implements DomainEvent {
}