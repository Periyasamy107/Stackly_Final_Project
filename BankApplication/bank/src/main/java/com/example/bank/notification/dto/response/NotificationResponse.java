package com.example.bank.notification.dto.response;

import com.example.bank.common.enums.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private NotificationType notificationType;

    private String title;

    private String message;

    private String referenceType;

    private Long referenceId;

    private boolean read;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;
}