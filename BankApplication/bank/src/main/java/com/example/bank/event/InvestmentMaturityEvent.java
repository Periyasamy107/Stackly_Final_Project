package com.example.bank.event;

import java.util.UUID;

public record InvestmentMaturityEvent(
        String eventId,
        Long userId,
        Long investmentId
) implements DomainEvent {

    public InvestmentMaturityEvent(
            Long userId,
            Long investmentId) {

        this(
                UUID.randomUUID().toString(),
                userId,
                investmentId
        );
    }
}