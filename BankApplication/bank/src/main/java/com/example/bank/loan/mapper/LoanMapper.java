package com.example.bank.loan.mapper;

import com.example.bank.loan.dto.request.LoanRequest;
import com.example.bank.loan.dto.response.LoanResponse;
import com.example.bank.loan.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(
            target = "customerId",
            source = "customer.id"
    )
    @Mapping(
            target = "disbursementAccountId",
            source = "disbursementAccount.id"
    )
    LoanResponse toResponse(Loan loan);

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "customer",
            ignore = true
    )
    @Mapping(
            target = "status",
            ignore = true
    )
    @Mapping(
            target = "rejectionReason",
            ignore = true
    )
    @Mapping(
            target = "disbursementAccount",
            ignore = true
    )
    @Mapping(
            target = "disbursementTransactionReference",
            ignore = true
    )
    @Mapping(
            target = "disbursedAt",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    @Mapping(
            target = "version",
            ignore = true
    )
    Loan toEntity(LoanRequest request);

    @Mapping(
            target = "id",
            ignore = true
    )
    @Mapping(
            target = "customer",
            ignore = true
    )
    @Mapping(
            target = "status",
            ignore = true
    )
    @Mapping(
            target = "rejectionReason",
            ignore = true
    )
    @Mapping(
            target = "disbursementAccount",
            ignore = true
    )
    @Mapping(
            target = "disbursementTransactionReference",
            ignore = true
    )
    @Mapping(
            target = "disbursedAt",
            ignore = true
    )
    @Mapping(
            target = "createdAt",
            ignore = true
    )
    @Mapping(
            target = "updatedAt",
            ignore = true
    )
    @Mapping(
            target = "version",
            ignore = true
    )
    void updateEntity(
            LoanRequest request,
            @MappingTarget Loan loan
    );
}