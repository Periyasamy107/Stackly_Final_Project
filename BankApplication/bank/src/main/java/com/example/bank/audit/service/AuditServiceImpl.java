package com.example.bank.audit.service;

import com.example.bank.audit.dto.response.AuditLogResponse;
import com.example.bank.audit.entity.AuditLog;
import com.example.bank.audit.repository.AuditLogRepository;
import com.example.bank.common.audit.AuditAction;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl
        implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(
            Long userId,
            String username,
            AuditAction action,
            String entityType,
            Boolean success,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        validateDateRange(
                fromDate,
                toDate
        );

        return auditLogRepository
                .search(
                        userId,
                        normalize(username),
                        action,
                        normalize(entityType),
                        success,
                        fromDate,
                        toDate,
                        pageable
                )
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getById(
            Long id) {

        if (id == null) {
            throw new BusinessException(
                    "Audit log id is required"
            );
        }

        AuditLog auditLog =
                auditLogRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Audit log not found: "
                                                + id
                                )
                        );

        return toResponse(auditLog);
    }

    @Override
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    public void record(
            AuditLogRecord record) {

        if (record == null
                || record.eventId() == null
                || record.eventId().isBlank()) {

            return;
        }

        AuditLog auditLog =
                AuditLog.builder()
                        .eventId(record.eventId())
                        .userId(record.userId())
                        .username(record.username())
                        .role(record.role())
                        .action(record.action())
                        .entityType(record.entityType())
                        .entityId(record.entityId())
                        .serviceName(record.serviceName())
                        .methodName(record.methodName())
                        .httpMethod(record.httpMethod())
                        .requestUri(record.requestUri())
                        .success(record.success())
                        .errorType(record.errorType())
                        .errorMessage(
                                sanitizeErrorMessage(
                                        record.errorMessage()
                                )
                        )
                        .build();


        auditLog.setCreatedBy(
                record.username()
        );

        auditLog.setUpdatedBy(
                record.username()
        );

        try {

            auditLogRepository.save(
                    auditLog
            );

        } catch (DataIntegrityViolationException ignored) {

            /*
             * event_id is unique.
             *
             * Duplicate delivery must never cause the original
             * business operation to fail.
             */
        }
    }

    private AuditLogResponse toResponse(
            AuditLog auditLog) {

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .eventId(auditLog.getEventId())
                .userId(auditLog.getUserId())
                .username(auditLog.getUsername())
                .role(auditLog.getRole())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .serviceName(auditLog.getServiceName())
                .methodName(auditLog.getMethodName())
                .httpMethod(auditLog.getHttpMethod())
                .requestUri(auditLog.getRequestUri())
                .success(auditLog.isSuccess())
                .errorType(auditLog.getErrorType())
                .errorMessage(auditLog.getErrorMessage())
                .createdAt(auditLog.getCreatedAt())
                .createdBy(auditLog.getCreatedBy())
                .updatedAt(auditLog.getUpdatedAt())
                .updatedBy(auditLog.getUpdatedBy())
                .build();
    }

    private void validateDateRange(
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new BusinessException(
                    "fromDate must be before or equal to toDate"
            );
        }
    }

    private String normalize(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        return value.trim();
    }

    private String sanitizeErrorMessage(
            String message) {

        if (message == null
                || message.isBlank()) {

            return null;
        }

        String sanitized =
                message
                        .replaceAll(
                                "[\\r\\n]+",
                                " "
                        )
                        .trim();

        sanitized =
                sanitized.replaceAll(
                        "(?i)(password|passwd|pwd)\\s*[:=]\\s*[^,;\\s]+",
                        "$1=[REDACTED]"
                );

        sanitized =
                sanitized.replaceAll(
                        "(?i)(authorization|proxy-authorization)\\s*[:=]\\s*[^,;\\s]+",
                        "$1=[REDACTED]"
                );

        sanitized =
                sanitized.replaceAll(
                        "(?i)(bearer)\\s+[A-Za-z0-9._~+/=-]+",
                        "$1 [REDACTED]"
                );

        sanitized =
                sanitized.replaceAll(
                        "(?i)(refresh[_-]?token|access[_-]?token|token)\\s*[:=]\\s*[^,;\\s]+",
                        "$1=[REDACTED]"
                );

        sanitized =
                sanitized.replaceAll(
                        "(?i)(api[_-]?key|secret[_-]?key|client[_-]?secret)\\s*[:=]\\s*[^,;\\s]+",
                        "$1=[REDACTED]"
                );

        sanitized =
                sanitized.replaceAll(
                        "(?i)(jdbc:[^\\s,;]+)",
                        "[REDACTED-JDBC-URL]"
                );

        sanitized =
                sanitized.replaceAll(
                        "(?i)(https?://[^\\s,;]+)",
                        "[REDACTED-URL]"
                );

        sanitized =
                sanitized.replaceAll(
                        "[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]",
                        ""
                );

        if (sanitized.length() > 1000) {

            sanitized =
                    sanitized.substring(
                            0,
                            1000
                    );
        }

        return sanitized;
    }

}