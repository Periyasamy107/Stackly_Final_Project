package com.example.bank.security.authorization;

import com.example.bank.account.entity.Account;
import com.example.bank.account.repository.AccountRepository;
import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountAuthorizationService {

    private final AccountRepository accountRepository;

    public boolean canAccessAccount(Long accountId) {

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

        return ownsAccount(accountId);
    }

    public boolean ownsAccount(Long accountId) {
        Authentication authentication = getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Long authenticatedUserId = getAuthenticatedUserId(authentication);

        if (authenticatedUserId == null) {
            return false;
        }

        return accountRepository.existsByIdAndCustomerUserId(
                accountId,
                authenticatedUserId
        );
    }

    public boolean canAccessCustomerAccounts(
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

        Long authenticatedUserId =
                getAuthenticatedUserId(authentication);

        if (authenticatedUserId == null) {
            return false;
        }

        return accountRepository
                .findByCustomerId(customerId)
                .stream()
                .findFirst()
                .map(Account::getCustomer)
                .map(customer ->
                        customer.getUser()
                                .getId()
                                .equals(authenticatedUserId)
                )
                .orElse(false);
    }

    private Long getAuthenticatedUserId(
            Authentication authentication) {

        Object principal =
                authentication.getPrincipal();

        /*
         * MVC / Session authentication
         */
        if (principal instanceof CustomUserDetails details) {

            return details.getUserId();
        }

        /*
         * REST / JWT authentication
         */
        if (principal instanceof Jwt jwt) {

            return jwt.getClaim("userId");
        }

        return null;
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