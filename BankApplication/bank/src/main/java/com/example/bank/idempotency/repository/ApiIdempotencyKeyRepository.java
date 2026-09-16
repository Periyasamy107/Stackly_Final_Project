package com.example.bank.idempotency.repository;

import com.example.bank.idempotency.entity.ApiIdempotencyKey;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ApiIdempotencyKeyRepository
        extends JpaRepository<ApiIdempotencyKey, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT i
            FROM ApiIdempotencyKey i
            JOIN FETCH i.user
            WHERE i.user.id = :userId
              AND i.idempotencyKey = :idempotencyKey
            """)
    Optional<ApiIdempotencyKey> findForUpdate(
            @Param("userId") Long userId,
            @Param("idempotencyKey") String idempotencyKey
    );

    @Modifying
    @Query(
            value = """
                    INSERT IGNORE INTO api_idempotency_keys (
                        user_id,
                        idempotency_key,
                        request_hash,
                        status,
                        expires_at
                    )
                    VALUES (
                        :userId,
                        :idempotencyKey,
                        :requestHash,
                        'IN_PROGRESS',
                        :expiresAt
                    )
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("userId") Long userId,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("requestHash") String requestHash,
            @Param("expiresAt") LocalDateTime expiresAt
    );

    @Modifying
    @Query("""
            UPDATE ApiIdempotencyKey i
            SET i.status = :status,
                i.httpStatus = :httpStatus,
                i.responseBody = :responseBody,
                i.completedAt = :completedAt
            WHERE i.id = :id
            """)
    int markCompleted(
            @Param("id") Long id,
            @Param("status")
            com.example.bank.common.enums.IdempotencyStatus status,
            @Param("httpStatus") Integer httpStatus,
            @Param("responseBody") String responseBody,
            @Param("completedAt")
            LocalDateTime completedAt
    );

    @Modifying
    @Query("""
            DELETE FROM ApiIdempotencyKey i
            WHERE i.user.id = :userId
              AND i.idempotencyKey = :idempotencyKey
            """)
    int deleteByUserIdAndKey(
            @Param("userId") Long userId,
            @Param("idempotencyKey") String idempotencyKey
    );

    @Modifying
    @Query("""
            DELETE FROM ApiIdempotencyKey i
            WHERE i.expiresAt < :now
            """)
    int deleteExpired(
            @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
        DELETE FROM ApiIdempotencyKey i
        WHERE i.expiresAt < :now
        """)
    int deleteExpiredKeys(
            @Param("now") LocalDateTime now
    );
}