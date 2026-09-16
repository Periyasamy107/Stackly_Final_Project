package com.example.bank.customer.service;

import com.example.bank.customer.dto.request.CustomerRequest;
import com.example.bank.customer.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(
            Long id,
            CustomerRequest request
    );

    CustomerResponse getCustomerById(Long id);

    CustomerResponse getCustomerByUserId(Long userId);

    List<CustomerResponse> getAllCustomers();

    void deleteCustomer(Long id);

    CustomerResponse deactivateCustomer(Long customerId);

    CustomerResponse activateCustomer(Long customerId);

}