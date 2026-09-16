package com.example.bank.reporting.service;

import com.example.bank.reporting.dto.request.ReportRequest;
import com.example.bank.reporting.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportingService {

    ReportQuery prepareQuery(
            ReportRequest request
    );

    Pageable createPageable(
            ReportRequest request
    );

    Page<CustomerReportResponse> customerReport(
            ReportRequest request
    );

    Page<AccountReportResponse> accountReport(
            ReportRequest request
    );

    Page<TransactionReportResponse> transactionReport(
            ReportRequest request
    );

    Page<LoanReportResponse> loanReport(
            ReportRequest request
    );

    Page<InvestmentReportResponse> investmentReport(
            ReportRequest request
    );
}