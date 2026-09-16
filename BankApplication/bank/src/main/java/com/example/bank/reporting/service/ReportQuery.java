package com.example.bank.reporting.service;


import com.example.bank.reporting.enums.ReportType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReportQuery(

        ReportType reportType,

        Long customerId,

        Long accountId,

        String accountNumber,

        String transactionType,

        String transactionStatus,

        LocalDate fromDate,

        LocalDate toDate,

        BigDecimal minAmount,

        BigDecimal maxAmount,

        String sortBy,

        String direction,

        Pageable pageable
) {
}
