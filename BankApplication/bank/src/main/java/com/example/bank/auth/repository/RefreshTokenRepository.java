package com.example.bank.auth.repository;

import com.example.bank.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM RefreshToken r
            JOIN FETCH r.user
            WHERE r.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Modifying
    @Query("""
            UPDATE RefreshToken r
            SET r.revokedAt = :revokedAt
            WHERE r.user.id = :userId
              AND r.revokedAt IS NULL
            """)
    int revokeAllByUserId(
            @Param("userId") Long userId,
            @Param("revokedAt") LocalDateTime revokedAt
    );

    @Modifying
    @Query("""
            DELETE FROM RefreshToken r
            WHERE r.expiresAt < :now
            """)
    int deleteExpiredTokens(
            @Param("now") LocalDateTime now
    );
}