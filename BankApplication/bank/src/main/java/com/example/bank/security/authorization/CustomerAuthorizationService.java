package com.example.bank.security.authorization;

import com.example.bank.customer.entity.Customer;
import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAuthorizationService {

    private final CustomerRepository customerRepository;

    public boolean isCurrentCustomer(Long customerId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        return customerRepository.findById(customerId)
                .map(customer ->
                        customer.getUser()
                                .getId()
                                .equals(
                                        details.getUserId()
                                )
                )
                .orElse(false);
    }

    public boolean canAccessCustomer(
            Long customerId,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        if (authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN"))) {

            return true;
        }

        if (authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_EMPLOYEE"))) {

            return true;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails user)) {
            return false;
        }

        return customerRepository
                .findById(customerId)
                .map(Customer::getUser)
                .map(userEntity ->
                        userEntity.getId()
                                .equals(user.getUserId()))
                .orElse(false);
    }

    public boolean canAccessCustomerByUserId(
            Long userId,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails user)) {
            return false;
        }

        return user.getUserId().equals(userId);
    }
}