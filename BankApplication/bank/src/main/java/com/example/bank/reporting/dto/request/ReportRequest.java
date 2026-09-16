package com.example.bank.reporting.dto.request;

import com.example.bank.reporting.enums.ReportType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    private ReportType reportType;

    private Long customerId;

    private Long accountId;

    private Long loanId;

    private Long investmentId;

    @Size(max = 50)
    private String loanType;

    @Size(max = 50)
    private String loanStatus;

    @Size(max = 50)
    private String investmentType;

    @Size(max = 50)
    private String investmentStatus;

    @Size(max = 50)
    private String accountNumber;

    @Size(max = 50)
    private String transactionType;

    @Size(max = 50)
    private String transactionStatus;

    private LocalDate fromDate;

    private LocalDate toDate;

    @DecimalMin(value = "0.00")
    private BigDecimal minAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal maxAmount;

    @Min(0)
    private Integer page;

    @Min(1)
    private Integer size;

    @Size(max = 50)
    private String sortBy;

    @Size(max = 10)
    private String direction;
}