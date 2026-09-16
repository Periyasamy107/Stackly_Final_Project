package com.example.bank.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(90)
@Slf4j
public class PerformanceAspect {

    private final long warningThresholdMillis;

    public PerformanceAspect(
            @Value(
                    "${bank.performance.slow-method-threshold-ms:1000}"
            )
            long warningThresholdMillis) {

        this.warningThresholdMillis =
                warningThresholdMillis;
    }

    @Around(
            "execution(public * com.example.bank..service..*(..))"
                    + " && !within(com.example.bank.audit..*)"
                    + " && !within(com.example.bank.notification..*)"
                    + " && !within(com.example.bank.idempotency..*)"
    )
    public Object measureServiceMethod(
            ProceedingJoinPoint joinPoint)
            throws Throwable {

        long start =
                System.nanoTime();

        try {

            return joinPoint.proceed();

        } finally {

            long durationNanos =
                    System.nanoTime() - start;

            long durationMillis =
                    TimeUnit.NANOSECONDS.toMillis(
                            durationNanos
                    );

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

            if (durationMillis >= warningThresholdMillis) {

                log.warn(
                        "Slow service method: {}.{}() took {} ms",
                        className,
                        methodName,
                        durationMillis
                );

            } else if (log.isDebugEnabled()) {

                log.debug(
                        "Service method performance: {}.{}() took {} ms",
                        className,
                        methodName,
                        durationMillis
                );
            }
        }
    }
}