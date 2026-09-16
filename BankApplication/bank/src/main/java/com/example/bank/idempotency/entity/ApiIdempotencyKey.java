package com.example.bank.idempotency.entity;

import com.example.bank.common.enums.IdempotencyStatus;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "api_idempotency_keys",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_api_idempotency_user_key",
                        columnNames = {
                                "user_id",
                                "idempotency_key"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_api_idempotency_expires_at",
                        columnList = "expires_at"
                ),
                @Index(
                        name = "idx_api_idempotency_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiIdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "idempotency_key",
            nullable = false,
            length = 100
    )
    private String idempotencyKey;

    @Column(
            name = "request_hash",
            nullable = false,
            length = 64,
            updatable = false
    )
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private IdempotencyStatus status;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Lob
    @Column(
            name = "response_body",
            columnDefinition = "LONGTEXT"
    )
    private String responseBody;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(
            name = "expires_at",
            nullable = false
    )
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = DateTimeUtil.nowUtc();
        }

        if (status == null) {
            status = IdempotencyStatus.IN_PROGRESS;
        }
    }
}