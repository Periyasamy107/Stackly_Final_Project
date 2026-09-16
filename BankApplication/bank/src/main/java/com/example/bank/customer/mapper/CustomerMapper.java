package com.example.bank.customer.mapper;

import com.example.bank.customer.dto.request.CustomerRequest;
import com.example.bank.customer.dto.response.CustomerResponse;
import com.example.bank.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "status", source = "status")
    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(
            CustomerRequest request,
            @MappingTarget Customer customer
    );

}