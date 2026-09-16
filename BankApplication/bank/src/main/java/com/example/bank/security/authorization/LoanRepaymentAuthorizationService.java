package com.example.bank.security.authorization;

import com.example.bank.account.entity.Account;
import com.example.bank.account.repository.AccountRepository;
import com.example.bank.loan.entity.Loan;
import com.example.bank.loan.repository.LoanRepository;
import com.example.bank.loanrepayment.entity.LoanRepayment;
import com.example.bank.loanrepayment.repository.LoanRepaymentRepository;
import com.example.bank.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanRepaymentAuthorizationService {

    private final LoanRepaymentRepository loanRepaymentRepository;
    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;

    public boolean canMakeRepayment(
            Long loanId,
            Long accountId) {

        Authentication authentication = getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN") ||
                hasRole(authentication, "EMPLOYEE")) {

            return belongsToLoanCustomer(loanId, accountId);
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        return belongsToLoanCustomer(loanId, accountId)
                && isCurrentCustomerLoan(loanId, details.getUserId())
                && isCurrentCustomerAccount(accountId, details.getUserId());
    }

    public boolean canAccessRepayment(Long repaymentId) {
        Authentication authentication = getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN") ||
                hasRole(authentication, "EMPLOYEE")) {

            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        return loanRepaymentRepository.findById(repaymentId)
                .map(LoanRepayment::getLoan)
                .map(Loan::getCustomer)
                .map(customer -> customer.getUser().getId())
                .map(details.getUserId()::equals)
                .orElse(false);
    }

    public boolean canAccessLoanRepayments(Long loanId) {
        Authentication authentication = getAuthentication();

        if (!isAuthenticated(authentication)) {
            return false;
        }

        if (hasRole(authentication, "ADMIN") ||
                hasRole(authentication, "EMPLOYEE")) {

            return true;
        }

        if (!hasRole(authentication, "CUSTOMER")) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomUserDetails details)) {
            return false;
        }

        return isCurrentCustomerLoan(
                loanId,
                details.getUserId()
        );
    }

    private boolean belongsToLoanCustomer(
            Long loanId,
            Long accountId) {

        Loan loan = loanRepository.findById(loanId)
                .orElse(null);

        Account account = accountRepository.findById(accountId)
                .orElse(null);

        if (loan == null || account == null) {
            return false;
        }

        return loan.getCustomer().getId()
                .equals(account.getCustomer().getId());
    }

    private boolean isCurrentCustomerLoan(
            Long loanId,
            Long userId) {

        return loanRepository.findById(loanId)
                .map(Loan::getCustomer)
                .map(customer -> customer.getUser())
                .map(user -> user.getId())
                .map(userId::equals)
                .orElse(false);
    }

    private boolean isCurrentCustomerAccount(
            Long accountId,
            Long userId) {

        return accountRepository.findById(accountId)
                .map(Account::getCustomer)
                .map(customer -> customer.getUser())
                .map(user -> user.getId())
                .map(userId::equals)
                .orElse(false);
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