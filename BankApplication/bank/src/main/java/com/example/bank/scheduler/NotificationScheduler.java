package com.example.bank.scheduler;

import com.example.bank.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;

    /**
     * Periodically checks notification storage and logs the current
     * notification count.
     *
     * No notifications are deleted automatically because the project
     * does not currently define a notification-retention policy.
     */
    @Scheduled(
            fixedDelayString = "${bank.scheduler.notification-maintenance-delay-ms:21600000}"
    )
    public void notificationMaintenance() {

        long notificationCount =
                notificationRepository.count();

        log.debug(
                "Notification maintenance completed. Current notification count: {}",
                notificationCount
        );
    }
}