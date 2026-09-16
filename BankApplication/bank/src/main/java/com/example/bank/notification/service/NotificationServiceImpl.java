package com.example.bank.notification.service;

import com.example.bank.common.enums.NotificationType;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.notification.dto.response.NotificationResponse;
import com.example.bank.notification.entity.Notification;
import com.example.bank.notification.repository.NotificationRepository;
import com.example.bank.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final UserRepository userRepository;

    @Override
    public List<NotificationResponse> getMyNotifications(
            Long userId) {

        validateUserId(userId);

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse>
    getMyUnreadNotifications(Long userId) {

        validateUserId(userId);

        return notificationRepository
                .findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse getMyNotification(
            Long userId,
            Long notificationId) {

        validateUserId(userId);
        validateNotificationId(notificationId);

        Notification notification =
                notificationRepository
                        .findByIdAndUserId(
                                notificationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found: "
                                                + notificationId
                                ));

        return toResponse(notification);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(
            Long userId,
            Long notificationId) {

        validateUserId(userId);
        validateNotificationId(notificationId);

        Notification notification =
                notificationRepository
                        .findByIdAndUserIdForUpdate(
                                notificationId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Notification not found: "
                                                + notificationId
                                ));

        if (notification.getReadAt() == null) {

            notification.setReadAt(
                    DateTimeUtil.nowUtc()
            );

            notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {

        validateUserId(userId);

        notificationRepository.markAllAsRead(
                userId,
                DateTimeUtil.nowUtc()
        );
    }

    @Override
    @Transactional
    public void createNotification(
            String eventId,
            Long userId,
            NotificationType notificationType,
            String title,
            String message,
            String referenceType,
            Long referenceId) {

        validateEventId(eventId);
        validateUserId(userId);

        if (notificationType == null) {
            throw new BusinessException(
                    "Notification type is required"
            );
        }

        if (title == null || title.isBlank()) {
            throw new BusinessException(
                    "Notification title is required"
            );
        }

        if (message == null || message.isBlank()) {
            throw new BusinessException(
                    "Notification message is required"
            );
        }

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(
                    "User not found: " + userId
            );
        }

        notificationRepository.insertIfAbsent(
                userId,
                eventId,
                notificationType.name(),
                title,
                message,
                referenceType,
                referenceId
        );
    }

    private NotificationResponse toResponse(
            Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .notificationType(
                        notification.getNotificationType()
                )
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(
                        notification.getReferenceType()
                )
                .referenceId(
                        notification.getReferenceId()
                )
                .read(
                        notification.getReadAt() != null
                )
                .readAt(
                        notification.getReadAt()
                )
                .createdAt(
                        notification.getCreatedAt()
                )
                .build();
    }

    private void validateUserId(Long userId) {

        if (userId == null) {
            throw new BusinessException(
                    "User id is required"
            );
        }
    }

    private void validateNotificationId(
            Long notificationId) {

        if (notificationId == null) {
            throw new BusinessException(
                    "Notification id is required"
            );
        }
    }

    private void validateEventId(String eventId) {

        if (eventId == null || eventId.isBlank()) {
            throw new BusinessException(
                    "Event id is required"
            );
        }
    }
}