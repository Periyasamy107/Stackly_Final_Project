package com.example.bank.security.authorization;

import com.example.bank.security.authentication.CustomUserDetails;
import com.example.bank.transaction.entity.Transaction;
import com.example.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionAuthorizationService {

    private final TransactionRepository transactionRepository;

    public boolean canAccessTransaction(Long transactionId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

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

        return transactionRepository.findById(transactionId)
                .map(Transaction::getAccount)
                .map(account ->
                        account.getCustomer()
                                .getUser()
                                .getId()
                                .equals(details.getUserId())
                )
                .orElse(false);
    }

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_" + role));
    }
}