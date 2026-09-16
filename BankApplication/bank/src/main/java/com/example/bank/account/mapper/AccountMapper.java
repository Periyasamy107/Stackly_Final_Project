package com.example.bank.account.mapper;

import com.example.bank.account.dto.request.AccountRequest;
import com.example.bank.account.dto.response.AccountResponse;
import com.example.bank.account.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "accountNumber", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Account toEntity(AccountRequest request);

    @Mapping(
            target = "customerId",
            source = "customer.id"
    )
    AccountResponse toResponse(Account account);
}