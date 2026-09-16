package com.example.bank.event;

import java.util.UUID;

public record CustomerCreatedEvent(
        String eventId,
        Long userId,
        Long customerId,
        String customerName
) implements DomainEvent {

    public CustomerCreatedEvent(
            Long userId,
            Long customerId,
            String customerName) {

        this(
                UUID.randomUUID().toString(),
                userId,
                customerId,
                customerName
        );
    }
}