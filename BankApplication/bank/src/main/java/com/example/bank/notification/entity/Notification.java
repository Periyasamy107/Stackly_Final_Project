package com.example.bank.notification.entity;

import com.example.bank.common.enums.NotificationType;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notifications_event_id",
                        columnNames = "event_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_notifications_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_notifications_user_read",
                        columnList = "user_id, read_at"
                ),
                @Index(
                        name = "idx_notifications_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

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
            name = "event_id",
            nullable = false,
            unique = true,
            length = 100,
            updatable = false
    )
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "notification_type",
            nullable = false,
            length = 50,
            updatable = false
    )
    private NotificationType notificationType;

    @Column(
            name = "title",
            nullable = false,
            length = 150,
            updatable = false
    )
    private String title;

    @Column(
            name = "message",
            nullable = false,
            length = 1000,
            updatable = false
    )
    private String message;

    @Column(
            name = "reference_type",
            length = 50,
            updatable = false
    )
    private String referenceType;

    @Column(
            name = "reference_id",
            updatable = false
    )
    private Long referenceId;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = DateTimeUtil.nowUtc();
        }
    }
}