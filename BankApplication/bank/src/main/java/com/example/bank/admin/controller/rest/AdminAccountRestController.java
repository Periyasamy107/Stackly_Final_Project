package com.example.bank.admin.controller.rest;

import com.example.bank.admin.service.AdminAccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
@Tag(
        name = "Admin Accounts",
        description = "Administrative account lifecycle operations"
)
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountRestController {

    private final AdminAccountService adminAccountService;

    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long userId) {

        adminAccountService.deactivateUser(
                userId
        );

        return ResponseEntity.noContent()
                .build();
    }
}