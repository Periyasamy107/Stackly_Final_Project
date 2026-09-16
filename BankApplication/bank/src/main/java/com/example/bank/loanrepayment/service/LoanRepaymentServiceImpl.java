package com.example.bank.loanrepayment.service;

import com.example.bank.account.entity.Account;
import com.example.bank.account.repository.AccountRepository;
import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.LoanRepaymentStatus;
import com.example.bank.common.enums.LoanStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.common.exception.ValidationException;
import com.example.bank.customer.service.CustomerLifecycleValidator;
import com.example.bank.loan.entity.Loan;
import com.example.bank.loan.repository.LoanRepository;
import com.example.bank.loanrepayment.dto.request.LoanRepaymentRequest;
import com.example.bank.loanrepayment.dto.response.LoanRepaymentResponse;
import com.example.bank.loanrepayment.entity.LoanRepayment;
import com.example.bank.loanrepayment.mapper.LoanRepaymentMapper;
import com.example.bank.loanrepayment.repository.LoanRepaymentRepository;
import com.example.bank.transaction.dto.request.TransactionRequest;
import com.example.bank.transaction.dto.response.TransactionResponse;
import com.example.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanRepaymentServiceImpl
        implements LoanRepaymentService {

    private final LoanRepaymentRepository loanRepaymentRepository;

    private final LoanRepository loanRepository;

    private final AccountRepository accountRepository;

    private final TransactionService transactionService;

    private final LoanRepaymentMapper loanRepaymentMapper;

    private final CustomerLifecycleValidator customerLifecycleValidator;

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "loanById",
            key = "#loanId"
    )
    public LoanRepaymentResponse makeRepayment(
            Long loanId,
            LoanRepaymentRequest request) {

        if (loanId == null) {
            throw new ValidationException(
                    "Loan id is required"
            );
        }

        if (request == null
                || request.getAccountId() == null) {

            throw new ValidationException(
                    "Repayment account id is required"
            );
        }

        if (request.getAmount() == null
                || request.getAmount().signum() <= 0) {

            throw new ValidationException(
                    "Repayment amount must be greater than zero"
            );
        }

        Loan loan =
                loanRepository.findByIdForUpdate(loanId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan not found with id: "
                                                + loanId
                                )
                        );

        customerLifecycleValidator.validateActive(
                loan.getCustomer()
        );

        if (loan.getStatus()
                != LoanStatus.ACTIVE) {

            throw new BusinessException(
                    "Only ACTIVE loans can receive repayments"
            );
        }

        if (loan.getDisbursedAt() == null
                || loan.getDisbursementTransactionReference()
                == null
                || loan.getDisbursementAccount() == null) {

            throw new BusinessException(
                    "Loan has not been disbursed"
            );
        }

        BigDecimal completedRepayments =
                loanRepaymentRepository
                        .sumCompletedRepaymentsByLoanId(
                                loanId
                        );

        if (completedRepayments == null) {
            completedRepayments =
                    BigDecimal.ZERO;
        }

        BigDecimal principal =
                loan.getPrincipalAmount();

        BigDecimal outstanding =
                principal.subtract(
                        completedRepayments
                );

        if (outstanding.signum() <= 0) {

            throw new BusinessException(
                    "Loan has no outstanding principal"
            );
        }

        BigDecimal repaymentAmount =
                request.getAmount();

        if (repaymentAmount.compareTo(
                outstanding
        ) > 0) {

            throw new BusinessException(
                    "Repayment amount cannot exceed outstanding principal of "
                            + outstanding
            );
        }

        Account account =
                accountRepository.findByIdForUpdate(
                                request.getAccountId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account not found with id: "
                                                + request.getAccountId()
                                )
                        );

        if (account.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    "Repayment account must be ACTIVE"
            );
        }

        if (account.getCustomer() == null
                || account.getCustomer().getId() == null
                || !account.getCustomer()
                .getId()
                .equals(
                        loan.getCustomer().getId()
                )) {

            throw new BusinessException(
                    "Repayment account does not belong to the loan customer"
            );
        }

        String transactionReference =
                "LOAN-REPAY-"
                        + loan.getId()
                        + "-"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20);

        TransactionRequest transactionRequest =
                TransactionRequest.builder()
                        .accountId(account.getId())
                        .amount(repaymentAmount)
                        .description(
                                "Loan repayment for loan id "
                                        + loan.getId()
                        )
                        .transactionReference(
                                transactionReference
                        )
                        .build();

        TransactionResponse transaction =
                transactionService.withdraw(
                        transactionRequest
                );

        LoanRepayment repayment =
                LoanRepayment.builder()
                        .loan(loan)
                        .account(account)
                        .transactionReference(
                                transaction.getTransactionReference()
                        )
                        .amount(repaymentAmount)
                        .status(
                                LoanRepaymentStatus.COMPLETED
                        )
                        .description(
                                "Loan repayment for loan id "
                                        + loan.getId()
                        )
                        .build();

        LoanRepayment savedRepayment =
                loanRepaymentRepository.save(
                        repayment
                );


        BigDecimal newOutstanding =
                outstanding.subtract(
                        repaymentAmount
                );

        if (newOutstanding.signum() == 0) {

            loan.setStatus(
                    LoanStatus.COMPLETED
            );

            loanRepository.save(loan);
        }

        return loanRepaymentMapper.toResponse(
                savedRepayment
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoanRepaymentResponse getRepaymentById(
            Long repaymentId) {

        LoanRepayment repayment =
                loanRepaymentRepository.findById(repaymentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan repayment not found with id: "
                                                + repaymentId
                                ));

        return loanRepaymentMapper.toResponse(
                repayment
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> getRepaymentsByLoanId(
            Long loanId) {

        if (!loanRepository.existsById(loanId)) {
            throw new ResourceNotFoundException(
                    "Loan not found with id: " + loanId
            );
        }

        return loanRepaymentRepository
                .findByLoanIdOrderByRepaymentDateDesc(loanId)
                .stream()
                .map(loanRepaymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanRepaymentResponse> getAllRepayments() {
        return loanRepaymentRepository.findAll()
                .stream()
                .map(loanRepaymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getOutstandingAmount(
            Long loanId) {

        if (loanId == null) {
            throw new ValidationException(
                    "Loan id is required"
            );
        }

        Loan loan =
                loanRepository.findById(loanId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Loan not found with id: "
                                                + loanId
                                )
                        );

        BigDecimal completedRepayments =
                loanRepaymentRepository
                        .sumCompletedRepaymentsByLoanId(
                                loanId
                        );

        if (completedRepayments == null) {
            completedRepayments =
                    BigDecimal.ZERO;
        }

        BigDecimal outstanding =
                loan.getPrincipalAmount()
                        .subtract(completedRepayments);

        if (outstanding.signum() < 0) {
            return BigDecimal.ZERO;
        }

        return outstanding;
    }

    private Loan findLoanForUpdate(Long loanId) {
        return loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Loan not found with id: " + loanId
                        ));
    }

    private void validateRepaymentAccount(
            Loan loan,
            Account account) {

        if (!account.getCustomer().getId()
                .equals(loan.getCustomer().getId())) {

            throw new BusinessException(
                    "Repayment account does not belong to the loan customer"
            );
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    "Loan repayment is allowed only from an active account"
            );
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Repayment amount must be greater than zero"
            );
        }

        if (amount.scale() > 4) {
            throw new BusinessException(
                    "Repayment amount cannot have more than 4 decimal places"
            );
        }
    }
}