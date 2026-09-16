package com.example.bank.audit.repository;

import com.example.bank.audit.entity.AuditLog;
import com.example.bank.common.audit.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    @Query("""
            SELECT a
            FROM AuditLog a
            WHERE (:userId IS NULL OR a.userId = :userId)
              AND (
                    :username IS NULL
                    OR LOWER(a.username)
                       LIKE LOWER(CONCAT('%', :username, '%'))
                  )
              AND (:action IS NULL OR a.action = :action)
              AND (
                    :entityType IS NULL
                    OR LOWER(a.entityType) = LOWER(:entityType)
                  )
              AND (:success IS NULL OR a.success = :success)
              AND (
                    :fromDate IS NULL
                    OR a.createdAt >= :fromDate
                  )
              AND (
                    :toDate IS NULL
                    OR a.createdAt < :toDate
                  )
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> search(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("action") AuditAction action,
            @Param("entityType") String entityType,
            @Param("success") Boolean success,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
}