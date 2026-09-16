package com.example.bank.loan.service;

import com.example.bank.account.entity.Account;
import com.example.bank.account.repository.AccountRepository;
import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.LoanStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.LoanEligibilityException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.common.exception.ValidationException;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.customer.entity.Customer;
import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.customer.service.CustomerLifecycleValidator;
import com.example.bank.event.LoanApprovedEvent;
import com.example.bank.loan.dto.request.LoanApprovalRequest;
import com.example.bank.loan.dto.request.LoanDisbursementRequest;
import com.example.bank.loan.dto.request.LoanRejectionRequest;
import com.example.bank.loan.dto.request.LoanRequest;
import com.example.bank.loan.dto.response.LoanEligibilityResponse;
import com.example.bank.loan.dto.response.LoanResponse;
import com.example.bank.loan.entity.Loan;
import com.example.bank.loan.mapper.LoanMapper;
import com.example.bank.loan.repository.LoanRepository;
import com.example.bank.loanrepayment.repository.LoanRepaymentRepository;
import com.example.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final LoanMapper loanMapper;
    private final TransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;
    private final LoanRepaymentRepository loanRepaymentRepository;
    private final CustomerLifecycleValidator customerLifecycleValidator;

    @Override
    @Transactional
    public LoanResponse applyForLoan(LoanRequest request) {
        validatePrincipalAmount(request.getPrincipalAmount());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));

        customerLifecycleValidator.validateActive(customer);

        LoanEligibilityResponse eligibility = checkEligibility(request.getCustomerId());

        if (!eligibility.isEligible()) {
            throw new LoanEligibilityException(String.join("; ", eligibility.getReasons()));
        }

        Loan loan = Loan.builder()
                .customer(customer)
                .loanType(request.getLoanType())
                .principalAmount(request.getPrincipalAmount())
                .status(LoanStatus.PENDING)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toResponse(savedLoan);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "loanById",
            key = "#loanId"
    )
    public LoanResponse approveLoan(Long loanId, LoanApprovalRequest request) {
        validateApprovalDates(request);
        validateInterestRate(request.getInterestRate());

        Loan loan = findLoanForUpdate(loanId);

        customerLifecycleValidator.validateActive(
                loan.getCustomer()
        );

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessException("Only pending loans can be approved");
        }

        loan.setInterestRate(request.getInterestRate());
        loan.setStartDate(request.getStartDate());
        loan.setEndDate(request.getEndDate());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setRejectionReason(null);

        eventPublisher.publishEvent(
                new LoanApprovedEvent(
                        loan.getCustomer().getUser().getId(),
                        loan.getId(),
                        loan.getLoanType(),
                        loan.getPrincipalAmount()
                )
        );

        return loanMapper.toResponse(loan);
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "loanById",
            key = "#loanId"
    )
    public LoanResponse rejectLoan(Long loanId, LoanRejectionRequest request) {
        Loan loan = findLoanForUpdate(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new BusinessException("Only pending loans can be rejected");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRejectionReason(request.getReason());

        return loanMapper.toResponse(loan);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "loanById",
            key = "#loanId"
    )
    public LoanResponse getLoanById(Long loanId) {
        Loan loan = findLoan(loanId);
        return loanMapper.toResponse(loan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }

        return loanRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll()
                .stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoanEligibilityResponse checkEligibility(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));

        List<String> reasons = new ArrayList<>();

        boolean hasActiveAccount = accountRepository
                .findByCustomerIdAndStatus(customer.getId(), AccountStatus.ACTIVE)
                .stream()
                .findAny()
                .isPresent();

        if (!hasActiveAccount) {
            reasons.add("Customer must have at least one active account");
        }

        if (reasons.isEmpty()) {
            reasons.add("Customer passed preliminary eligibility checks");

            return LoanEligibilityResponse.builder()
                    .customerId(customerId)
                    .eligible(true)
                    .reasons(reasons)
                    .build();
        }

        return LoanEligibilityResponse.builder()
                .customerId(customerId)
                .eligible(false)
                .reasons(reasons)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "loanById",
            key = "#loanId"
    )
    public LoanResponse completeLoan(Long loanId) {

        Loan loan = findLoanForUpdate(loanId);

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException(
                    "Only active loans can be completed"
            );
        }

        if (loan.getEndDate() == null) {
            throw new BusinessException(
                    "Loan end date is required"
            );
        }

        if (loan.getEndDate().isAfter(DateTimeUtil.todayUtc())) {
            throw new BusinessException(
                    "Loan cannot be completed before end date"
            );
        }

        BigDecimal completedRepayments =
                loanRepaymentRepository
                        .sumCompletedRepaymentsByLoanId(
                                loanId
                        );

        if (completedRepayments == null) {
            completedRepayments = BigDecimal.ZERO;
        }

        BigDecimal outstandingPrincipal =
                loan.getPrincipalAmount()
                        .subtract(completedRepayments);

        if (outstandingPrincipal.signum() > 0) {
            throw new BusinessException(
                    "Loan cannot be completed while outstanding principal remains: "
                            + outstandingPrincipal
            );
        }

        loan.setStatus(LoanStatus.COMPLETED);

        Loan completedLoan =
                loanRepository.save(loan);

        return loanMapper.toResponse(
                completedLoan
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "loanById",
            key = "#loanId"
    )
    public LoanResponse disburseLoan(
            Long loanId,
            LoanDisbursementRequest request) {

        if (loanId == null) {
            throw new ValidationException(
                    "Loan id is required"
            );
        }

        if (request == null
                || request.getAccountId() == null) {

            throw new ValidationException(
                    "Disbursement account id is required"
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
                    "Only ACTIVE loans can be disbursed"
            );
        }

        if (loan.getDisbursedAt() != null
                || loan.getDisbursementTransactionReference()
                != null
                || loan.getDisbursementAccount() != null) {

            throw new BusinessException(
                    "Loan has already been disbursed"
            );
        }

        BigDecimal principalAmount =
                loan.getPrincipalAmount();

        if (principalAmount == null
                || principalAmount.signum() <= 0) {

            throw new BusinessException(
                    "Loan principal amount must be greater than zero"
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
                    "Disbursement account must be ACTIVE"
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
                    "Disbursement account does not belong to the loan customer"
            );
        }

        String transactionReference =
                "LOAN-" + loan.getId()
                        + "-"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20);

        transactionService.creditLoanDisbursement(
                account.getId(),
                principalAmount,
                transactionReference,
                "Loan disbursement for loan id "
                        + loan.getId()
        );

        loan.setDisbursementAccount(account);

        loan.setDisbursementTransactionReference(
                transactionReference
        );

        loan.setDisbursedAt(
                DateTimeUtil.nowUtc()
        );

        Loan savedLoan =
                loanRepository.save(loan);

        return loanMapper.toResponse(savedLoan);
    }

    private Loan findLoan(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with id: " + loanId));
    }

    private Loan findLoanForUpdate(Long loanId) {
        return loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found with id: " + loanId));
    }

    private void validatePrincipalAmount(BigDecimal principalAmount) {
        if (principalAmount == null || principalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Principal amount must be greater than zero");
        }

        if (principalAmount.scale() > 4) {
            throw new BusinessException("Principal amount cannot have more than 4 decimal places");
        }
    }

    private void validateInterestRate(BigDecimal interestRate) {
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Interest rate cannot be negative");
        }

        if (interestRate.scale() > 4) {
            throw new BusinessException("Interest rate cannot have more than 4 decimal places");
        }
    }

    private void validateApprovalDates(LoanApprovalRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BusinessException("Start date and end date are required");
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date cannot be before start date");
        }
    }
}