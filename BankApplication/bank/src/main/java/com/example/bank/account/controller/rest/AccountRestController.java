package com.example.bank.account.controller.rest;

import com.example.bank.account.dto.request.AccountRequest;
import com.example.bank.account.dto.response.AccountResponse;
import com.example.bank.account.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(
        name = "Accounts",
        description = "Bank account management operations"
)
public class AccountRestController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request) {

        AccountResponse response =
                accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@accountAuthorizationService.canAccessAccount(#id)"
    )
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.getAccountById(id)
        );
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize(
            "@accountAuthorizationService" +
                    ".canAccessCustomerAccounts(#customerId)"
    )
    public ResponseEntity<List<AccountResponse>>
    getAccountsByCustomerId(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                accountService.getAccountsByCustomerId(
                        customerId
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<AccountResponse>>
    getAllAccounts() {

        return ResponseEntity.ok(
                accountService.getAllAccounts()
        );
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AccountResponse> blockAccount(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.blockAccount(id)
        );
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AccountResponse> activateAccount(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.activateAccount(id)
        );
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AccountResponse> deactivateAccount(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.deactivateAccount(id)
        );
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AccountResponse> closeAccount(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                accountService.closeAccount(id)
        );
    }
}