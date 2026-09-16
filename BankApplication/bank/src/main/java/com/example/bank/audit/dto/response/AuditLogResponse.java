package com.example.bank.audit.dto.response;

import com.example.bank.common.audit.AuditAction;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;

    private String eventId;

    private Long userId;

    private String username;

    private String role;

    private AuditAction action;

    private String entityType;

    private Long entityId;

    private String serviceName;

    private String methodName;

    private String httpMethod;

    private String requestUri;

    private boolean success;

    private String errorType;

    private String errorMessage;

    private LocalDateTime createdAt;

    private String createdBy;

    private LocalDateTime updatedAt;

    private String updatedBy;
}