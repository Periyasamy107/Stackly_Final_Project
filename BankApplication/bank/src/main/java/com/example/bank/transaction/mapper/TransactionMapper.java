package com.example.bank.transaction.mapper;

import com.example.bank.transaction.dto.response.TransactionResponse;
import com.example.bank.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(
            target = "accountId",
            source = "account.id"
    )
    @Mapping(
            target = "accountNumber",
            source = "account.accountNumber"
    )
    TransactionResponse toResponse(
            Transaction transaction
    );
}