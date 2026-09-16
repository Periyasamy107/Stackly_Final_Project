package com.example.bank.reporting.controller.rest;

import com.example.bank.reporting.dto.request.ReportRequest;
import com.example.bank.reporting.dto.response.*;
import com.example.bank.reporting.service.ReportingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
@Tag(
        name = "Reports",
        description = "Read-only banking reports"
)
public class ReportingRestController {

    private final ReportingService reportingService;

    @PostMapping("/customers")
    public ResponseEntity<Page<CustomerReportResponse>>
    customerReport(
            @Valid @RequestBody ReportRequest request) {

        request.setReportType(
                com.example.bank.reporting.enums.ReportType.CUSTOMER
        );

        return ResponseEntity.ok(
                reportingService.customerReport(
                        request
                )
        );
    }

    @PostMapping("/accounts")
    public ResponseEntity<Page<AccountReportResponse>>
    accountReport(
            @Valid @RequestBody ReportRequest request) {

        request.setReportType(
                com.example.bank.reporting.enums.ReportType.ACCOUNT
        );

        return ResponseEntity.ok(
                reportingService.accountReport(
                        request
                )
        );
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Page<TransactionReportResponse>> transactionReport(
            @Valid ReportRequest request
    ) {
        return ResponseEntity.ok(
                reportingService.transactionReport(request)
        );
    }

    @GetMapping("/loans")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Page<LoanReportResponse>> loanReport(
            @Valid ReportRequest request
    ) {
        return ResponseEntity.ok(
                reportingService.loanReport(request)
        );
    }

    @GetMapping("/investments")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<Page<InvestmentReportResponse>> investmentReport(
            @Valid ReportRequest request
    ) {
        return ResponseEntity.ok(
                reportingService.investmentReport(request)
        );
    }
}