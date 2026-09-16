package com.example.bank.scheduler;

import com.example.bank.idempotency.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyCleanupScheduler {

    private final IdempotencyService idempotencyService;

    /**
     * Removes idempotency records that have passed their
     * configured expiration time.
     */
    @Scheduled(
            fixedDelayString =
                    "${bank.scheduler.idempotency-cleanup-delay-ms:21600000}"
    )
    public void cleanupExpiredIdempotencyKeys() {

        try {

            idempotencyService.cleanupExpiredKeys();

        } catch (Exception exception) {

            log.error(
                    "Idempotency cleanup failed",
                    exception
            );
        }
    }
}