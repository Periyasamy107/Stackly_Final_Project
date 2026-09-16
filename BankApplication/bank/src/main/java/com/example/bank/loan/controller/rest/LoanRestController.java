package com.example.bank.loan.controller.rest;

import com.example.bank.common.response.ApiResponse;
import com.example.bank.loan.dto.request.LoanApprovalRequest;
import com.example.bank.loan.dto.request.LoanDisbursementRequest;
import com.example.bank.loan.dto.request.LoanRejectionRequest;
import com.example.bank.loan.dto.request.LoanRequest;
import com.example.bank.loan.dto.response.LoanEligibilityResponse;
import com.example.bank.loan.dto.response.LoanResponse;
import com.example.bank.loan.service.LoanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(
        name = "Loans",
        description = "Loan lifecycle and repayment operations"
)
public class LoanRestController {

    private final LoanService loanService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanResponse> applyForLoan(
            @Valid @RequestBody LoanRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        loanService.applyForLoan(request)
                );
    }

    @GetMapping("/{loanId}")
    @PreAuthorize(
            "@loanAuthorizationService"
                    + ".canAccessLoan(#loanId)"
    )
    public ResponseEntity<LoanResponse> getLoanById(
            @PathVariable Long loanId) {

        return ResponseEntity.ok(
                loanService.getLoanById(loanId)
        );
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize(
            "@loanAuthorizationService"
                    + ".canAccessCustomerLoans(#customerId)"
    )
    public ResponseEntity<List<LoanResponse>>
    getLoansByCustomerId(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                loanService.getLoansByCustomerId(
                        customerId
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE')"
    )
    public ResponseEntity<List<LoanResponse>>
    getAllLoans() {

        return ResponseEntity.ok(
                loanService.getAllLoans()
        );
    }

    @GetMapping("/eligibility/{customerId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE') or " +
                    "@customerAuthorizationService"
                    + ".isCurrentCustomer(#customerId)"
    )
    public ResponseEntity<LoanEligibilityResponse>
    checkEligibility(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                loanService.checkEligibility(
                        customerId
                )
        );
    }

    @PatchMapping("/{loanId}/approve")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE')"
    )
    public ResponseEntity<LoanResponse> approveLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody
            LoanApprovalRequest request) {

        return ResponseEntity.ok(
                loanService.approveLoan(
                        loanId,
                        request
                )
        );
    }

    @PatchMapping("/{loanId}/reject")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE')"
    )
    public ResponseEntity<LoanResponse> rejectLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody
            LoanRejectionRequest request) {

        return ResponseEntity.ok(
                loanService.rejectLoan(
                        loanId,
                        request
                )
        );
    }

    @PatchMapping("/{loanId}/complete")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'EMPLOYEE')"
    )
    public ResponseEntity<LoanResponse> completeLoan(
            @PathVariable Long loanId) {

        return ResponseEntity.ok(
                loanService.completeLoan(
                        loanId
                )
        );
    }

    @PostMapping("/{loanId}/disbursements")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<LoanResponse>> disburseLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanDisbursementRequest request) {

        LoanResponse response =
                loanService.disburseLoan(
                        loanId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}