package com.example.bank.investmentperformance.mapper;

import com.example.bank.investmentperformance.dto.response.InvestmentPerformanceResponse;
import com.example.bank.investmentperformance.entity.InvestmentPerformance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvestmentPerformanceMapper {

    @Mapping(
            target = "investmentId",
            source = "investment.id"
    )
    @Mapping(
            target = "customerId",
            source = "investment.customer.id"
    )
    @Mapping(
            target = "accountId",
            source = "investment.account.id"
    )
    InvestmentPerformanceResponse toResponse(
            InvestmentPerformance performance
    );
}