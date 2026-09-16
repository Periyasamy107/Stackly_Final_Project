package com.example.bank.idempotency.service;

import com.example.bank.common.enums.IdempotencyStatus;
import com.example.bank.common.exception.IdempotencyException;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.idempotency.entity.ApiIdempotencyKey;
import com.example.bank.idempotency.repository.ApiIdempotencyKeyRepository;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.concurrent.Callable;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl
        implements IdempotencyService {

    @Value(
            "${bank.idempotency.ttl-minutes:2}"
    )
    private long idempotencyTtlMinutes;

    private final ApiIdempotencyKeyRepository repository;

    private final ObjectMapper objectMapper;

    private final ApiIdempotencyKeyRepository idempotencyKeyRepository;

    @Override
    @Transactional
    public Object execute(
            Long userId,
            String idempotencyKey,
            String requestHash,
            Class<?> returnType,
            Type genericReturnType,
            Callable<Object> operation)
            throws Exception {

        validateUserId(userId);
        validateKey(idempotencyKey);
        validateHash(requestHash);

        LocalDateTime now =
                DateTimeUtil.nowUtc();

        LocalDateTime expiresAt =
                now.plusMinutes(
                        idempotencyTtlMinutes
                );

        repository.insertIfAbsent(
                userId,
                idempotencyKey,
                requestHash,
                expiresAt
        );

        ApiIdempotencyKey record =
                repository.findForUpdate(
                                userId,
                                idempotencyKey
                        )
                        .orElseThrow(() ->
                                new IdempotencyException(
                                        "Unable to create idempotency record"
                                )
                        );

        if (record.getExpiresAt()
                .isBefore(now)) {

            repository.deleteByUserIdAndKey(
                    userId,
                    idempotencyKey
            );

            repository.insertIfAbsent(
                    userId,
                    idempotencyKey,
                    requestHash,
                    expiresAt
            );

            record =
                    repository.findForUpdate(
                                    userId,
                                    idempotencyKey
                            )
                            .orElseThrow(() ->
                                    new IdempotencyException(
                                            "Unable to recreate expired idempotency record"
                                    )
                            );
        }

        if (!record.getRequestHash()
                .equals(requestHash)) {

            throw new IdempotencyException(
                    "Idempotency key has already been used "
                            + "with a different request"
            );
        }


        if (record.getStatus()
                == IdempotencyStatus.COMPLETED) {

            return replay(
                    record.getResponseBody(),
                    record.getHttpStatus(),
                    returnType,
                    genericReturnType
            );
        }

        Object result;

        try {

            result =
                    operation.call();

        } catch (Exception exception) {

            throw exception;

        } catch (Error error) {

            throw error;
        }

        ResponseEntity<?> response =
                toResponseEntity(result);

        String responseBody =
                serializeResponseBody(
                        response.getBody()
                );

        repository.markCompleted(
                record.getId(),
                IdempotencyStatus.COMPLETED,
                response.getStatusCode().value(),
                responseBody,
                DateTimeUtil.nowUtc()
        );

        return result;
    }

    @Override
    public String generateRequestHash(
            Long userId,
            String idempotencyKey,
            String httpMethod,
            String requestUri,
            String queryString,
            Object[] arguments) {

        try {

            String argumentsJson =
                    objectMapper.writeValueAsString(
                            arguments == null
                                    ? new Object[0]
                                    : arguments
                    );

            String payload =
                    String.join(
                            "|",
                            String.valueOf(userId),
                            idempotencyKey,
                            httpMethod == null
                                    ? ""
                                    : httpMethod,
                            requestUri == null
                                    ? ""
                                    : requestUri,
                            queryString == null
                                    ? ""
                                    : queryString,
                            argumentsJson
                    );

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            payload.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (Exception exception) {

            throw new IdempotencyException(
                    "Unable to generate request fingerprint"
            );
        }
    }

    @Override
    public ResponseEntity<?> replay(
            String responseBody,
            Integer httpStatus,
            Class<?> returnType,
            Type genericReturnType) {

        if (!ResponseEntity.class
                .isAssignableFrom(returnType)) {

            return ResponseEntity
                    .status(
                            httpStatus == null
                                    ? 200
                                    : httpStatus
                    )
                    .body(
                            deserialize(
                                    responseBody,
                                    genericReturnType
                            )
                    );
        }

        if (responseBody == null
                || responseBody.isBlank()
                || "null".equals(responseBody)) {

            return ResponseEntity
                    .status(
                            httpStatus == null
                                    ? 200
                                    : httpStatus
                    )
                    .build();
        }

        Type bodyType =
                extractResponseEntityBodyType(
                        genericReturnType
                );

        Object body =
                deserialize(
                        responseBody,
                        bodyType
                );

        return ResponseEntity
                .status(
                        httpStatus == null
                                ? 200
                                : httpStatus
                )
                .body(body);
    }

    @Override
    @Transactional
    public void cleanupExpiredKeys() {

        int deletedCount =
                idempotencyKeyRepository.deleteExpiredKeys(
                        DateTimeUtil.nowUtc()
                );

        log.info(
                "Idempotency cleanup completed. Deleted {} expired keys",
                deletedCount
        );
    }

    private ResponseEntity<?> toResponseEntity(
            Object result) {

        if (result instanceof ResponseEntity<?> response) {
            return response;
        }

        return ResponseEntity.ok(result);
    }

    private String serializeResponseBody(
            Object body) {

        if (body == null) {
            return null;
        }

        try {

            return objectMapper.writeValueAsString(
                    body
            );

        } catch (Exception exception) {

            throw new IdempotencyException(
                    "Unable to store idempotent response"
            );
        }
    }

    private Object deserialize(
            String body,
            Type type) {

        try {

            JavaType javaType =
                    objectMapper.getTypeFactory()
                            .constructType(type);

            return objectMapper.readValue(
                    body,
                    javaType
            );

        } catch (Exception exception) {

            throw new IdempotencyException(
                    "Unable to replay idempotent response"
            );
        }
    }

    private Type extractResponseEntityBodyType(
            Type returnType) {

        if (returnType instanceof ParameterizedType parameterizedType) {

            Type rawType =
                    parameterizedType.getRawType();

            if (rawType instanceof Class<?> rawClass
                    && ResponseEntity.class
                    .isAssignableFrom(rawClass)) {

                Type[] arguments =
                        parameterizedType
                                .getActualTypeArguments();

                if (arguments.length == 1) {
                    return arguments[0];
                }
            }
        }

        return Object.class;
    }

    private void validateUserId(
            Long userId) {

        if (userId == null) {
            throw new IdempotencyException(
                    "Authenticated user is required"
            );
        }
    }

    private void validateKey(
            String idempotencyKey) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()) {

            throw new IdempotencyException(
                    "Idempotency-Key header is required"
            );
        }

        if (idempotencyKey.length() > 100) {

            throw new IdempotencyException(
                    "Idempotency-Key cannot exceed 100 characters"
            );
        }
    }

    private void validateHash(
            String requestHash) {

        if (requestHash == null
                || requestHash.isBlank()) {

            throw new IdempotencyException(
                    "Request fingerprint is required"
            );
        }
    }
}