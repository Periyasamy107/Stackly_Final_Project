package com.example.bank.notification.controller.rest;

import com.example.bank.notification.dto.response.NotificationResponse;
import com.example.bank.notification.service.NotificationService;
import com.example.bank.security.authentication.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(
        name = "Notifications",
        description = "User notification operations"
)
public class NotificationRestController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getMyNotifications(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(
                        currentUserId(authentication)
                )
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>>
    getMyUnreadNotifications(
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService
                        .getMyUnreadNotifications(
                                currentUserId(authentication)
                        )
        );
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse>
    getMyNotification(
            @PathVariable Long notificationId,
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.getMyNotification(
                        currentUserId(authentication),
                        notificationId
                )
        );
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse>
    markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        currentUserId(authentication),
                        notificationId
                )
        );
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Authentication authentication) {

        notificationService.markAllAsRead(
                currentUserId(authentication)
        );

        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authenticated user is required"
            );
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Object userIdClaim =
                    jwtAuthenticationToken
                            .getToken()
                            .getClaims()
                            .get("userId");

            if (userIdClaim instanceof Number number) {
                return number.longValue();
            }

            if (userIdClaim instanceof String value) {

                try {
                    return Long.parseLong(value);

                } catch (NumberFormatException ignored) {

                    throw new IllegalStateException(
                            "Invalid authenticated user ID"
                    );
                }
            }
        }

        if (authentication.getPrincipal()
                instanceof CustomUserDetails userDetails) {

            return userDetails.getUserId();
        }

        throw new IllegalStateException(
                "Unable to determine authenticated user"
        );
    }


}