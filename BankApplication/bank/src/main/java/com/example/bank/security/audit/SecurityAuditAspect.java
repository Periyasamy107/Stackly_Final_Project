package com.example.bank.security.audit;

import com.example.bank.common.audit.AuditAction;
import com.example.bank.event.AuditEvent;
import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityAuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleAuthorizationDenied(
            AuthorizationDeniedEvent event) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        Long userId = null;
        String username = null;
        String role = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication
                instanceof AnonymousAuthenticationToken)) {

            username =
                    authentication.getName();

            Object principal =
                    authentication.getPrincipal();

            if (principal
                    instanceof CustomUserDetails details) {

                userId =
                        details.getUserId();

                role =
                        details.getRole();
            }
        }

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder
                                .getRequestAttributes();

        String httpMethod = null;
        String requestUri = null;

        if (attributes != null
                && attributes.getRequest() != null) {

            httpMethod =
                    attributes
                            .getRequest()
                            .getMethod();

            requestUri =
                    attributes
                            .getRequest()
                            .getRequestURI();
        }

        eventPublisher.publishEvent(
                new AuditEvent(
                        UUID.randomUUID().toString(),
                        userId,
                        username,
                        role,
                        AuditAction.ACCESS_DENIED,
                        "SECURITY",
                        null,
                        "Security",
                        "authorizationDenied",
                        httpMethod,
                        requestUri,
                        false,
                        event.getClass().getName(),
                        "Authorization denied"
                )
        );
    }
}
