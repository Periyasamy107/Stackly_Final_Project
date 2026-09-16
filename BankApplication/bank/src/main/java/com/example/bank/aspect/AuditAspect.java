package com.example.bank.aspect;

import com.example.bank.common.audit.AuditAction;
import com.example.bank.event.AuditEvent;
import com.example.bank.security.authentication.CustomUserDetails;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.UUID;

@Aspect
@Component
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    public AuditAspect(
            ApplicationEventPublisher eventPublisher) {

        this.eventPublisher =
                eventPublisher;
    }

    @Around(
            "execution(public * com.example.bank..service..*(..)) "
                    + "&& !within(com.example.bank.audit..*)"
    )
    public Object auditServiceMethod(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        Method method =
                ((MethodSignature)
                        joinPoint.getSignature())
                        .getMethod();

        AuditContext context =
                buildContext(
                        joinPoint,
                        method
                );

        try {

            Object result =
                    joinPoint.proceed();

            publish(
                    context,
                    true,
                    null
            );

            return result;

        } catch (Throwable exception) {

            publish(
                    context,
                    false,
                    exception
            );

            throw exception;
        }
    }

    private void publish(
            AuditContext context,
            boolean success,
            Throwable exception) {

        eventPublisher.publishEvent(
                new AuditEvent(
                        UUID.randomUUID().toString(),
                        context.userId(),
                        context.username(),
                        context.role(),
                        context.action(),
                        context.entityType(),
                        context.entityId(),
                        context.serviceName(),
                        context.methodName(),
                        context.httpMethod(),
                        context.requestUri(),
                        success,
                        exception == null
                                ? null
                                : exception
                                .getClass()
                                .getName(),
                        exception == null
                                ? null
                                : exception.getMessage()
                )
        );
    }

    private AuditContext buildContext(
            ProceedingJoinPoint joinPoint,
            Method method) {

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


        if (username == null) {

            username =
                    extractUsernameFromArguments(
                            joinPoint.getArgs()
                    );
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

        String serviceName =
                joinPoint
                        .getTarget()
                        .getClass()
                        .getSimpleName()
                        .replace(
                                "Impl",
                                ""
                        );

        String methodName =
                method.getName();

        return new AuditContext(
                userId,
                username,
                role,
                classify(methodName),
                serviceName,
                methodName,
                extractEntityId(
                        joinPoint.getArgs()
                ),
                httpMethod,
                requestUri,
                entityType(serviceName)
        );
    }

    private AuditAction classify(
            String methodName) {

        String name =
                methodName.toLowerCase();

        if (name.contains("login")) {
            return AuditAction.LOGIN;
        }

        if (name.contains("logout")) {
            return AuditAction.LOGOUT;
        }

        if (name.contains("password")) {
            return AuditAction.PASSWORD_CHANGE;
        }

        if (name.contains("approve")) {
            return AuditAction.APPROVE;
        }

        if (name.contains("reject")) {
            return AuditAction.REJECT;
        }

        if (name.contains("disburse")) {
            return AuditAction.DISBURSE;
        }

        if (name.contains("settle")) {
            return AuditAction.SETTLEMENT;
        }

        if (name.contains("mature")) {
            return AuditAction.MATURITY;
        }

        if (name.contains("transfer")) {
            return AuditAction.TRANSFER;
        }

        if (name.contains("deposit")) {
            return AuditAction.DEPOSIT;
        }

        if (name.contains("withdraw")) {
            return AuditAction.WITHDRAWAL;
        }

        if (name.contains("repay")) {
            return AuditAction.REPAYMENT;
        }

        if (name.contains("invest")) {
            return AuditAction.INVESTMENT;
        }

        if (name.contains("activate")) {
            return AuditAction.ACTIVATE;
        }

        if (name.contains("deactivate")) {
            return AuditAction.DEACTIVATE;
        }

        if (name.startsWith("delete")) {
            return AuditAction.DELETE;
        }

        if (name.startsWith("update")) {
            return AuditAction.UPDATE;
        }

        if (name.startsWith("create")) {
            return AuditAction.CREATE;
        }

        if (name.startsWith("get")
                || name.startsWith("find")
                || name.startsWith("list")
                || name.startsWith("search")
                || name.startsWith("count")
                || name.startsWith("exists")) {

            return AuditAction.READ;
        }

        return AuditAction.UPDATE;
    }

    private String entityType(
            String serviceName) {

        if (serviceName.endsWith("Service")) {

            return serviceName.substring(
                    0,
                    serviceName.length()
                            - "Service".length()
            );
        }

        return serviceName;
    }

    private Long extractEntityId(
            Object[] args) {

        if (args == null) {
            return null;
        }


        for (Object arg : args) {

            if (arg instanceof Long id) {
                return id;
            }
        }


        for (Object arg : args) {

            Long id =
                    readId(arg);

            if (id != null) {
                return id;
            }
        }

        return null;
    }

    private Long readId(
            Object argument) {

        if (argument == null) {
            return null;
        }

        try {

            Method getter =
                    argument
                            .getClass()
                            .getMethod("getId");

            Object value =
                    getter.invoke(argument);

            return value instanceof Long id
                    ? id
                    : null;

        } catch (ReflectiveOperationException ignored) {
            // DTO does not expose an id.
        }

        if (argument
                .getClass()
                .isRecord()) {

            for (RecordComponent component :
                    argument
                            .getClass()
                            .getRecordComponents()) {

                if ("id".equalsIgnoreCase(
                        component.getName())
                        && component
                        .getType()
                        .equals(Long.class)) {

                    try {

                        Object value =
                                component
                                        .getAccessor()
                                        .invoke(argument);

                        return value instanceof Long id
                                ? id
                                : null;

                    } catch (ReflectiveOperationException ignored) {
                        return null;
                    }
                }
            }
        }

        return null;
    }

    private String extractUsernameFromArguments(
            Object[] args) {

        if (args == null) {
            return null;
        }

        for (Object arg : args) {

            if (arg == null) {
                continue;
            }

            try {

                Method getter =
                        arg.getClass()
                                .getMethod(
                                        "getUsername"
                                );

                Object value =
                        getter.invoke(arg);

                if (value instanceof String username
                        && !username.isBlank()) {

                    return username;
                }

            } catch (ReflectiveOperationException ignored) {
                // Argument does not expose username.
            }
        }

        return null;
    }

    private record AuditContext(

            Long userId,

            String username,

            String role,

            AuditAction action,

            String serviceName,

            String methodName,

            Long entityId,

            String httpMethod,

            String requestUri,

            String entityType
    ) {
    }
}