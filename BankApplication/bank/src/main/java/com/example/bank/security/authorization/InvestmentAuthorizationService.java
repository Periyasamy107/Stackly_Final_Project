package com.example.bank.security.authorization;

import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.investment.entity.Investment;
import com.example.bank.investment.repository.InvestmentRepository;
import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestmentAuthorizationService {

    private final InvestmentRepository investmentRepository;
    private final CustomerRepository customerRepository;

    public boolean canAccessInvestment(Long investmentId) {

        Authentication authentication = getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN")
                || hasRole(authentication, "EMPLOYEE")) {
            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        Long currentUserId = getCurrentUserId(authentication);

        if (currentUserId == null) {
            return false;
        }

        return investmentRepository.findById(investmentId)
                .map(Investment::getCustomer)
                .map(customer -> customer.getUser().getId())
                .map(userId -> userId.equals(currentUserId))
                .orElse(false);
    }

    public boolean canAccessCustomerInvestments(Long customerId) {

        Authentication authentication = getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN")
                || hasRole(authentication, "EMPLOYEE")) {
            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        Long currentUserId = getCurrentUserId(authentication);

        if (currentUserId == null) {
            return false;
        }

        return customerRepository.findById(customerId)
                .map(customer -> customer.getUser().getId())
                .map(userId -> userId.equals(currentUserId))
                .orElse(false);
    }

    public boolean isCurrentCustomer(Long customerId) {

        Authentication authentication = getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        Long currentUserId = getCurrentUserId(authentication);

        if (currentUserId == null) {
            return false;
        }

        return customerRepository.findById(customerId)
                .map(customer -> customer.getUser().getId())
                .map(userId -> userId.equals(currentUserId))
                .orElse(false);
    }

    private Long getCurrentUserId(
            Authentication authentication) {

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {

            Object userIdClaim =
                    jwtAuthenticationToken
                            .getToken()
                            .getClaims()
                            .get("userId");

            if (userIdClaim instanceof Number number) {
                return number.longValue();
            }

            if (userIdClaim instanceof String value) {
                try {
                    return Long.valueOf(value);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails details) {
            return details.getUserId();
        }

        return null;
    }

    private Authentication getAuthentication() {

        return SecurityContextHolder
                .getContext()
                .getAuthentication();
    }

    private boolean isAuthenticated(
            Authentication authentication) {

        return authentication != null
                && authentication.isAuthenticated();
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