package com.example.bank.audit.controller.rest;

import com.example.bank.audit.dto.response.AuditLogResponse;
import com.example.bank.audit.service.AuditService;
import com.example.bank.common.audit.AuditAction;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(
        name = "Audit Logs",
        description = "Administrative audit log operations"
)
public class AuditRestController {

    private final AuditService auditService;

    @GetMapping
    public Page<AuditLogResponse> search(
            @RequestParam(required = false)
            Long userId,

            @RequestParam(required = false)
            String username,

            @RequestParam(required = false)
            AuditAction action,

            @RequestParam(required = false)
            String entityType,

            @RequestParam(required = false)
            Boolean success,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime toDate,

            @PageableDefault(
                    size = 50,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return auditService.search(
                userId,
                username,
                action,
                entityType,
                success,
                fromDate,
                toDate,
                pageable
        );
    }

    @GetMapping("/{id}")
    public AuditLogResponse getById(
            @PathVariable Long id) {

        return auditService.getById(id);
    }
}