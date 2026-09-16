package com.example.bank.customer.service;

import com.example.bank.common.enums.CustomerStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.customer.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerLifecycleValidator {

    public void validateActive(Customer customer) {

        if (customer == null) {
            throw new BusinessException(
                    "Customer is required"
            );
        }

        if (customer.getStatus()
                != CustomerStatus.ACTIVE) {

            throw new BusinessException(
                    "Customer is inactive. Financial operations are not permitted"
            );
        }
    }
}