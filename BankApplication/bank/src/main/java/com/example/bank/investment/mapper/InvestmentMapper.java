package com.example.bank.investment.mapper;

import com.example.bank.investment.dto.response.InvestmentResponse;
import com.example.bank.investment.entity.Investment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvestmentMapper {

    @Mapping(
            target = "customerId",
            source = "customer.id"
    )
    @Mapping(
            target = "accountId",
            source = "account.id"
    )
    @Mapping(
            target = "accountNumber",
            source = "account.accountNumber"
    )
    @Mapping(
            target = "settlementAccountId",
            source = "settlementAccount.id"
    )
    InvestmentResponse toResponse(
            Investment investment
    );
}