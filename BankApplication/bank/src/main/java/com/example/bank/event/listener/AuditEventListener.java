package com.example.bank.event.listener;

import com.example.bank.audit.service.AuditService;
import com.example.bank.event.AuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditService auditService;

    @Async("bankTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMPLETION,
            fallbackExecution = true
    )
    public void handle(
            AuditEvent event) {

        auditService.record(
                new AuditService.AuditLogRecord(
                        event.eventId(),
                        event.userId(),
                        event.username(),
                        event.role(),
                        event.action(),
                        event.entityType(),
                        event.entityId(),
                        event.serviceName(),
                        event.methodName(),
                        event.httpMethod(),
                        event.requestUri(),
                        event.success(),
                        event.errorType(),
                        event.errorMessage()
                )
        );
    }
}