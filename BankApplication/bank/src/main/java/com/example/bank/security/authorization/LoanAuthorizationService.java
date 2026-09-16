package com.example.bank.security.authorization;

import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.loan.entity.Loan;
import com.example.bank.loan.repository.LoanRepository;
import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanAuthorizationService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;

    public boolean canAccessLoan(Long loanId) {

        Authentication authentication =
                getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        if (hasRole(authentication, "ADMIN") ||
                hasRole(authentication, "EMPLOYEE")) {

            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        return loanRepository.findById(loanId)
                .map(Loan::getCustomer)
                .map(customer ->
                        customer.getUser()
                                .getId()
                                .equals(
                                        details.getUserId()
                                )
                )
                .orElse(false);
    }

    public boolean canAccessCustomerLoans(
            Long customerId) {

        Authentication authentication =
                getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        if (hasRole(authentication, "ADMIN") ||
                hasRole(authentication, "EMPLOYEE")) {

            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        return customerRepository
                .findById(customerId)
                .map(customer ->
                        customer.getUser()
                                .getId()
                                .equals(
                                        details.getUserId()
                                )
                )
                .orElse(false);
    }

    private Authentication getAuthentication() {

        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_" + role)
                );
    }
}