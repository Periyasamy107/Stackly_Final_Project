package com.example.bank.security.authorization;

import com.example.bank.investment.entity.Investment;
import com.example.bank.investmentperformance.entity.InvestmentPerformance;
import com.example.bank.investmentperformance.repository.InvestmentPerformanceRepository;
import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestmentPerformanceAuthorizationService {

    private final InvestmentPerformanceRepository performanceRepository;

    public boolean canAccessPerformance(Long performanceId) {
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

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        return performanceRepository.findById(performanceId)
                .map(InvestmentPerformance::getInvestment)
                .map(Investment::getCustomer)
                .map(customer -> customer.getUser().getId())
                .map(userId -> userId.equals(details.getUserId()))
                .orElse(false);
    }

    public boolean canAccessInvestmentPerformance(Long investmentId) {
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

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        return performanceRepository.findFirstByInvestmentIdOrderByPerformanceDateDesc(investmentId)
                .map(InvestmentPerformance::getInvestment)
                .map(Investment::getCustomer)
                .map(customer -> customer.getUser().getId())
                .map(userId -> userId.equals(details.getUserId()))
                .orElse(false);
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated();
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_" + role));
    }
}