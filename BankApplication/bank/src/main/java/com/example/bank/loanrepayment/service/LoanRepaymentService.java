package com.example.bank.loanrepayment.service;

import com.example.bank.loanrepayment.dto.request.LoanRepaymentRequest;
import com.example.bank.loanrepayment.dto.response.LoanRepaymentResponse;

import java.math.BigDecimal;
import java.util.List;

public interface LoanRepaymentService {

    LoanRepaymentResponse makeRepayment(
            Long loanId,
            LoanRepaymentRequest request
    );

    LoanRepaymentResponse getRepaymentById(
            Long repaymentId
    );

    List<LoanRepaymentResponse> getRepaymentsByLoanId(
            Long loanId
    );

    List<LoanRepaymentResponse> getAllRepayments();

    BigDecimal getOutstandingAmount(Long loanId);

}