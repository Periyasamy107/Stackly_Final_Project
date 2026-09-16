package com.example.bank.investment.controller.rest;

import com.example.bank.investment.dto.request.InvestmentRequest;
import com.example.bank.investment.dto.response.InvestmentResponse;
import com.example.bank.investment.service.InvestmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
@Tag(
        name = "Investments",
        description = "Investment lifecycle and performance operations"
)
public class InvestmentRestController {

    private final InvestmentService investmentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<InvestmentResponse> createInvestment(
            @Valid @RequestBody InvestmentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        investmentService.createInvestment(
                                request
                        )
                );
    }

    @GetMapping("/{investmentId}")
    @PreAuthorize(
            "@investmentAuthorizationService"
                    + ".canAccessInvestment(#investmentId)"
    )
    public ResponseEntity<InvestmentResponse> getInvestmentById(
            @PathVariable Long investmentId) {

        return ResponseEntity.ok(
                investmentService.getInvestmentById(
                        investmentId
                )
        );
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize(
            "@investmentAuthorizationService"
                    + ".canAccessCustomerInvestments(#customerId)"
    )
    public ResponseEntity<List<InvestmentResponse>>
    getInvestmentsByCustomerId(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                investmentService.getInvestmentsByCustomerId(
                        customerId
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<List<InvestmentResponse>>
    getAllInvestments() {

        return ResponseEntity.ok(
                investmentService.getAllInvestments()
        );
    }

    @PatchMapping("/{investmentId}/mature")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvestmentResponse> matureInvestment(
            @PathVariable Long investmentId) {

        return ResponseEntity.ok(
                investmentService.matureInvestment(
                        investmentId
                )
        );
    }

    @PatchMapping("/{investmentId}/settle")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvestmentResponse> settleInvestment(
            @PathVariable Long investmentId) {

        return ResponseEntity.ok(
                investmentService.settleInvestment(
                        investmentId
                )
        );
    }
}