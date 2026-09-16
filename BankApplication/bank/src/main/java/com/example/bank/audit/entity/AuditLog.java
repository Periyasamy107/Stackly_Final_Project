package com.example.bank.audit.entity;

import com.example.bank.common.audit.AuditAction;
import com.example.bank.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "audit_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_audit_logs_event_id",
                        columnNames = "event_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_audit_logs_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_audit_logs_action",
                        columnList = "action"
                ),
                @Index(
                        name = "idx_audit_logs_entity",
                        columnList = "entity_type, entity_id"
                ),
                @Index(
                        name = "idx_audit_logs_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_audit_logs_success",
                        columnList = "success"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            length = 100,
            updatable = false
    )
    private String eventId;

    @Column(name = "user_id")
    private Long userId;

    @Column(
            name = "username",
            length = 100
    )
    private String username;

    @Column(
            name = "role",
            length = 30
    )
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action",
            nullable = false,
            length = 40,
            updatable = false
    )
    private AuditAction action;

    @Column(
            name = "entity_type",
            length = 100,
            updatable = false
    )
    private String entityType;

    @Column(
            name = "entity_id",
            updatable = false
    )
    private Long entityId;

    @Column(
            name = "service_name",
            nullable = false,
            length = 150,
            updatable = false
    )
    private String serviceName;

    @Column(
            name = "method_name",
            nullable = false,
            length = 150,
            updatable = false
    )
    private String methodName;

    @Column(
            name = "http_method",
            length = 20,
            updatable = false
    )
    private String httpMethod;

    @Column(
            name = "request_uri",
            length = 1000,
            updatable = false
    )
    private String requestUri;

    @Column(
            name = "success",
            nullable = false,
            updatable = false
    )
    private boolean success;

    @Column(
            name = "error_type",
            length = 200,
            updatable = false
    )
    private String errorType;

    @Column(
            name = "error_message",
            length = 1000,
            updatable = false
    )
    private String errorMessage;
}