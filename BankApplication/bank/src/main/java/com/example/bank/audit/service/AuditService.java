package com.example.bank.audit.service;

import com.example.bank.audit.dto.response.AuditLogResponse;
import com.example.bank.common.audit.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditService {

    Page<AuditLogResponse> search(
            Long userId,
            String username,
            AuditAction action,
            String entityType,
            Boolean success,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    );

    AuditLogResponse getById(
            Long id
    );

    void record(
            AuditLogRecord record
    );

    record AuditLogRecord(

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
}