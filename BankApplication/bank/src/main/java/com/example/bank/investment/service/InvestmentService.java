package com.example.bank.investment.service;

import com.example.bank.investment.dto.request.InvestmentRequest;
import com.example.bank.investment.dto.response.InvestmentResponse;

import java.util.List;

public interface InvestmentService {

    InvestmentResponse createInvestment(
            InvestmentRequest request
    );

    InvestmentResponse getInvestmentById(
            Long investmentId
    );

    List<InvestmentResponse> getInvestmentsByCustomerId(
            Long customerId
    );

    List<InvestmentResponse> getAllInvestments();

    InvestmentResponse matureInvestment(
            Long investmentId
    );

    InvestmentResponse settleInvestment(
            Long investmentId
    );
}