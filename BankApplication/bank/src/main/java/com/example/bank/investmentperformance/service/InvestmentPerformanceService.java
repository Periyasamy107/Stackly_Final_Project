package com.example.bank.investmentperformance.service;

import com.example.bank.investmentperformance.dto.request.InvestmentPerformanceRequest;
import com.example.bank.investmentperformance.dto.response.InvestmentPerformanceResponse;

import java.util.List;

public interface InvestmentPerformanceService {

    InvestmentPerformanceResponse recordPerformance(
            InvestmentPerformanceRequest request
    );

    InvestmentPerformanceResponse getPerformanceById(
            Long performanceId
    );

    List<InvestmentPerformanceResponse>
    getInvestmentPerformance(
            Long investmentId
    );

    InvestmentPerformanceResponse getLatestPerformance(
            Long investmentId
    );
}