package com.example.bank.investmentperformance.controller.rest;

import com.example.bank.investmentperformance.dto.request.InvestmentPerformanceRequest;
import com.example.bank.investmentperformance.dto.response.InvestmentPerformanceResponse;
import com.example.bank.investmentperformance.service.InvestmentPerformanceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investment-performance")
@RequiredArgsConstructor
@Tag(
        name = "Investment Performance",
        description = "Investment performance operations"
)
public class InvestmentPerformanceRestController {

    private final InvestmentPerformanceService performanceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<InvestmentPerformanceResponse> recordPerformance(
            @Valid @RequestBody InvestmentPerformanceRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        performanceService.recordPerformance(
                                request
                        )
                );
    }

    @GetMapping("/{performanceId}")
    @PreAuthorize(
            "@investmentPerformanceAuthorizationService"
                    + ".canAccessPerformance(#performanceId)"
    )
    public ResponseEntity<InvestmentPerformanceResponse> getPerformanceById(
            @PathVariable Long performanceId) {

        return ResponseEntity.ok(
                performanceService.getPerformanceById(
                        performanceId
                )
        );
    }

    @GetMapping("/investment/{investmentId}")
    @PreAuthorize(
            "@investmentPerformanceAuthorizationService"
                    + ".canAccessInvestmentPerformance(#investmentId)"
    )
    public ResponseEntity<List<InvestmentPerformanceResponse>>
    getInvestmentPerformance(
            @PathVariable Long investmentId) {

        return ResponseEntity.ok(
                performanceService.getInvestmentPerformance(
                        investmentId
                )
        );
    }

    @GetMapping("/investment/{investmentId}/latest")
    @PreAuthorize(
            "@investmentPerformanceAuthorizationService"
                    + ".canAccessInvestmentPerformance(#investmentId)"
    )
    public ResponseEntity<InvestmentPerformanceResponse>
    getLatestPerformance(
            @PathVariable Long investmentId) {

        return ResponseEntity.ok(
                performanceService.getLatestPerformance(
                        investmentId
                )
        );
    }
}