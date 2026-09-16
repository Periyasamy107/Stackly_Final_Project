package com.example.bank.aspect;

import com.example.bank.common.exception.IdempotencyException;
import com.example.bank.idempotency.filter.IdempotencyKeyFilter;
import com.example.bank.idempotency.service.IdempotencyService;
import com.example.bank.security.authentication.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class IdempotencyAspect {

    private final IdempotencyService idempotencyService;

    private final HttpServletRequest request;

    @Around(
            "execution(public * com.example.bank..controller.rest..*(..))"
                    + " && ("
                    + "@annotation(org.springframework.web.bind.annotation.PostMapping)"
                    + " || @annotation(org.springframework.web.bind.annotation.PutMapping)"
                    + " || @annotation(org.springframework.web.bind.annotation.PatchMapping)"
                    + " || @annotation(org.springframework.web.bind.annotation.DeleteMapping)"
                    + ")"
                    + " && !within(com.example.bank.auth.controller.rest..*)"
                    + " && !within(com.example.bank.file.controller.rest..*)"
                    + " && !within(com.example.bank.audit.controller.rest..*)"
                    + " && !within(com.example.bank.notification.controller.rest..*)"
    )
    public Object enforceIdempotency(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        String idempotencyKey =
                (String) request.getAttribute(
                        IdempotencyKeyFilter.REQUEST_ATTRIBUTE
                );

        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {

            throw new IdempotencyException(
                    "Idempotency-Key header is required "
                            + "for mutating REST operations"
            );
        }

        Long userId =
                currentUserId();

        String requestHash =
                idempotencyService.generateRequestHash(
                        userId,
                        idempotencyKey,
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getQueryString(),
                        joinPoint.getArgs()
                );

        Method method =
                ((MethodSignature)
                        joinPoint.getSignature())
                        .getMethod();

        return idempotencyService.execute(
                userId,
                idempotencyKey,
                requestHash,
                method.getReturnType(),
                method.getGenericReturnType(),
                () -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
        );

    }



    private Long currentUserId() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IdempotencyException(
                    "Authenticated user is required "
                            + "for idempotent REST operations"
            );
        }

        if (authentication
                instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Object userId =
                    jwtAuthenticationToken
                            .getToken()
                            .getClaim("userId");

            if (userId instanceof Number number) {
                return number.longValue();
            }

            if (userId instanceof String userIdString) {
                try {
                    return Long.valueOf(userIdString);
                } catch (NumberFormatException exception) {
                    throw new IdempotencyException(
                            "Authenticated JWT contains an invalid userId"
                    );
                }
            }
        }

        if (authentication.getPrincipal()
                instanceof CustomUserDetails details) {

            return details.getUserId();
        }

        throw new IdempotencyException(
                "Authenticated user identity could not be resolved"
        );
    }
}