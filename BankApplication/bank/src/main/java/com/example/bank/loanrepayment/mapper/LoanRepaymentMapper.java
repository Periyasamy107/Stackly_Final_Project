package com.example.bank.loanrepayment.mapper;

import com.example.bank.loanrepayment.dto.response.LoanRepaymentResponse;
import com.example.bank.loanrepayment.entity.LoanRepayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanRepaymentMapper {

    @Mapping(
            target = "loanId",
            source = "loan.id"
    )
    @Mapping(
            target = "accountId",
            source = "account.id"
    )
    @Mapping(
            target = "accountNumber",
            source = "account.accountNumber"
    )
    LoanRepaymentResponse toResponse(
            LoanRepayment loanRepayment
    );
}