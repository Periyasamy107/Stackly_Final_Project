package com.example.bank.notification.service;

import com.example.bank.common.enums.NotificationType;
import com.example.bank.notification.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications(
            Long userId
    );

    List<NotificationResponse> getMyUnreadNotifications(
            Long userId
    );

    NotificationResponse getMyNotification(
            Long userId,
            Long notificationId
    );

    NotificationResponse markAsRead(
            Long userId,
            Long notificationId
    );

    void markAllAsRead(
            Long userId
    );

    void createNotification(
            String eventId,
            Long userId,
            NotificationType notificationType,
            String title,
            String message,
            String referenceType,
            Long referenceId
    );
}