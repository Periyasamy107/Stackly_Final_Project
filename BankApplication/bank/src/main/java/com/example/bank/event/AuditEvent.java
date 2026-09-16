package com.example.bank.event;

import com.example.bank.common.audit.AuditAction;

public record AuditEvent(

        String eventId,

        Long userId,

        String username,

        String role,

        AuditAction action,

        String entityType,

        Long entityId,

        String serviceName,

        String methodName,

        String httpMethod,

        String requestUri,

        boolean success,

        String errorType,

        String errorMessage
) {
}