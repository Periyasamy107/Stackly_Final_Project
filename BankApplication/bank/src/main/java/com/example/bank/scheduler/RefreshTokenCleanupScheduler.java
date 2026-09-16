package com.example.bank.scheduler;

import com.example.bank.auth.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenService refreshTokenService;

    /**
     * Removes refresh tokens that have passed their expiration time.
     *
     * Revoked but unexpired tokens are deliberately retained so that
     * refresh-token reuse detection continues to work until the token
     * naturally expires.
     */
    @Scheduled(
            fixedDelayString =
                    "${bank.scheduler.refresh-token-cleanup-delay-ms:21600000}"
    )
    public void cleanupExpiredRefreshTokens() {

        try {

            refreshTokenService.cleanupExpiredTokens();

        } catch (Exception exception) {

            log.error(
                    "Refresh token cleanup failed",
                    exception
            );
        }
    }
}