package com.example.bank.loanrepayment.controller.rest;

import com.example.bank.loanrepayment.dto.request.LoanRepaymentRequest;
import com.example.bank.loanrepayment.dto.response.LoanRepaymentResponse;
import com.example.bank.loanrepayment.service.LoanRepaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-repayments")
@RequiredArgsConstructor
@Tag(
        name = "Loan Repayments",
        description = "Loan repayment operations"
)
public class LoanRepaymentRestController {

    private final LoanRepaymentService loanRepaymentService;

    @PostMapping("/loan/{loanId}")
    @PreAuthorize(
            "@loanRepaymentAuthorizationService"
                    + ".canMakeRepayment(#loanId, #request.accountId)"
    )
    public ResponseEntity<LoanRepaymentResponse> makeRepayment(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanRepaymentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        loanRepaymentService.makeRepayment(
                                loanId,
                                request
                        )
                );
    }

    @GetMapping("/{repaymentId}")
    @PreAuthorize(
            "@loanRepaymentAuthorizationService"
                    + ".canAccessRepayment(#repaymentId)"
    )
    public ResponseEntity<LoanRepaymentResponse> getRepaymentById(
            @PathVariable Long repaymentId) {

        return ResponseEntity.ok(
                loanRepaymentService.getRepaymentById(
                        repaymentId
                )
        );
    }

    @GetMapping("/loan/{loanId}")
    @PreAuthorize(
            "@loanRepaymentAuthorizationService"
                    + ".canAccessLoanRepayments(#loanId)"
    )
    public ResponseEntity<List<LoanRepaymentResponse>>
    getRepaymentsByLoanId(
            @PathVariable Long loanId) {

        return ResponseEntity.ok(
                loanRepaymentService.getRepaymentsByLoanId(
                        loanId
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE')"
    )
    public ResponseEntity<List<LoanRepaymentResponse>>
    getAllRepayments() {

        return ResponseEntity.ok(
                loanRepaymentService.getAllRepayments()
        );
    }
}