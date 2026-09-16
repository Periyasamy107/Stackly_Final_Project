package com.example.bank.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(100)
@Slf4j
public class LoggingAspect {

    @Around(
            "execution(public * com.example.bank..service..*(..))"
                    + " && !within(com.example.bank.audit..*)"
                    + " && !within(com.example.bank.notification..*)"
                    + " && !within(com.example.bank.idempotency..*)"
    )
    public Object logServiceMethod(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        Method method =
                ((MethodSignature)
                        joinPoint.getSignature())
                        .getMethod();

        String className =
                joinPoint
                        .getTarget()
                        .getClass()
                        .getSimpleName();

        String methodName =
                method.getName();

        if (log.isDebugEnabled()) {

            log.debug(
                    "Service method started: {}.{}()",
                    className,
                    methodName
            );
        }

        try {

            Object result =
                    joinPoint.proceed();

            if (log.isDebugEnabled()) {

                log.debug(
                        "Service method completed: {}.{}()",
                        className,
                        methodName
                );
            }

            return result;

        } catch (Throwable exception) {

            log.error(
                    "Service method failed: {}.{}() - {}",
                    className,
                    methodName,
                    exception.getClass().getSimpleName()
            );

            throw exception;
        }
    }
}