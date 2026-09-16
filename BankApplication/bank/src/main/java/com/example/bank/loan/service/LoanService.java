package com.example.bank.loan.service;

import com.example.bank.loan.dto.request.LoanApprovalRequest;
import com.example.bank.loan.dto.request.LoanDisbursementRequest;
import com.example.bank.loan.dto.request.LoanRejectionRequest;
import com.example.bank.loan.dto.request.LoanRequest;
import com.example.bank.loan.dto.response.LoanEligibilityResponse;
import com.example.bank.loan.dto.response.LoanResponse;

import java.util.List;

public interface LoanService {

    LoanResponse applyForLoan(
            LoanRequest request
    );

    LoanResponse approveLoan(
            Long loanId,
            LoanApprovalRequest request
    );

    LoanResponse rejectLoan(
            Long loanId,
            LoanRejectionRequest request
    );

    LoanResponse getLoanById(
            Long loanId
    );

    List<LoanResponse> getLoansByCustomerId(
            Long customerId
    );

    List<LoanResponse> getAllLoans();

    LoanEligibilityResponse checkEligibility(
            Long customerId
    );

    LoanResponse completeLoan(
            Long loanId
    );

    LoanResponse disburseLoan(
            Long loanId,
            LoanDisbursementRequest request
    );
}