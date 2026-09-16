package com.example.bank.notification.repository;

import com.example.bank.notification.entity.Notification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<Notification>
    findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(
            Long userId
    );

    Optional<Notification> findByIdAndUserId(
            Long id,
            Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT n
            FROM Notification n
            WHERE n.id = :id
              AND n.user.id = :userId
            """)
    Optional<Notification> findByIdAndUserIdForUpdate(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    @Modifying
    @Query("""
            UPDATE Notification n
            SET n.readAt = :readAt
            WHERE n.user.id = :userId
              AND n.readAt IS NULL
            """)
    int markAllAsRead(
            @Param("userId") Long userId,
            @Param("readAt") LocalDateTime readAt
    );

    boolean existsByEventId(String eventId);

    @Modifying
    @Query(
            value = """
                    INSERT IGNORE INTO notifications (
                        user_id,
                        event_id,
                        notification_type,
                        title,
                        message,
                        reference_type,
                        reference_id
                    )
                    VALUES (
                        :userId,
                        :eventId,
                        :notificationType,
                        :title,
                        :message,
                        :referenceType,
                        :referenceId
                    )
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("userId") Long userId,
            @Param("eventId") String eventId,
            @Param("notificationType") String notificationType,
            @Param("title") String title,
            @Param("message") String message,
            @Param("referenceType") String referenceType,
            @Param("referenceId") Long referenceId
    );
}