package com.example.bank.transaction.controller.rest;

import com.example.bank.transaction.dto.request.TransactionRequest;
import com.example.bank.transaction.dto.request.TransferRequest;
import com.example.bank.transaction.dto.response.TransactionResponse;
import com.example.bank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(
        name = "Transactions",
        description = "Deposits, withdrawals and transfers"
)
public class TransactionRestController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE')"
    )
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody TransactionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionService.deposit(request)
                );
    }

    @PostMapping("/withdraw")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE')"
    )
    public ResponseEntity<TransactionResponse> withdraw(
            @Valid @RequestBody TransactionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionService.withdraw(request)
                );
    }

    @PostMapping("/transfer")
    @PreAuthorize(
            "hasRole('CUSTOMER') and " +
                    "@accountAuthorizationService" +
                    ".ownsAccount(#request.sourceAccountId)"
    )
    public ResponseEntity<List<TransactionResponse>>
    transfer(
            @Valid @RequestBody TransferRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionService.transfer(request)
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@transactionAuthorizationService" +
                    ".canAccessTransaction(#id)"
    )
    public ResponseEntity<TransactionResponse>
    getTransactionById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                transactionService
                        .getTransactionById(id)
        );
    }

    @GetMapping("/account/{accountId}")
    @PreAuthorize(
            "@accountAuthorizationService" +
                    ".canAccessAccount(#accountId)"
    )
    public ResponseEntity<List<TransactionResponse>>
    getAccountTransactions(
            @PathVariable Long accountId) {

        return ResponseEntity.ok(
                transactionService
                        .getAccountTransactions(accountId)
        );
    }

    @PostMapping("/{id}/reverse")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE')"
    )
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionService
                                .reverseTransaction(id)
                );
    }
}