package com.example.bank.idempotency.service;

import org.springframework.http.ResponseEntity;

import java.util.concurrent.Callable;

public interface IdempotencyService {

    Object execute(
            Long userId,
            String idempotencyKey,
            String requestHash,
            Class<?> returnType,
            java.lang.reflect.Type genericReturnType,
            Callable<Object> operation
    ) throws Exception;

    String generateRequestHash(
            Long userId,
            String idempotencyKey,
            String httpMethod,
            String requestUri,
            String queryString,
            Object[] arguments
    );

    ResponseEntity<?> replay(
            String responseBody,
            Integer httpStatus,
            Class<?> returnType,
            java.lang.reflect.Type genericReturnType
    );

    void cleanupExpiredKeys();
}