package com.example.bank.investment.service;

import com.example.bank.account.entity.Account;
import com.example.bank.account.repository.AccountRepository;
import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.InvestmentStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.customer.entity.Customer;
import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.customer.service.CustomerLifecycleValidator;
import com.example.bank.event.InvestmentMaturityEvent;
import com.example.bank.investment.dto.request.InvestmentRequest;
import com.example.bank.investment.dto.response.InvestmentResponse;
import com.example.bank.investment.entity.Investment;
import com.example.bank.investment.mapper.InvestmentMapper;
import com.example.bank.investment.repository.InvestmentRepository;
import com.example.bank.investmentperformance.entity.InvestmentPerformance;
import com.example.bank.investmentperformance.repository.InvestmentPerformanceRepository;
import com.example.bank.security.authorization.InvestmentAuthorizationService;
import com.example.bank.transaction.dto.request.TransactionRequest;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl
        implements InvestmentService {

    private static final int MONEY_SCALE = 4;

    private final InvestmentRepository investmentRepository;

    private final CustomerRepository customerRepository;

    private final AccountRepository accountRepository;

    private final InvestmentPerformanceRepository performanceRepository;

    private final TransactionService transactionService;

    private final InvestmentMapper investmentMapper;

    private final InvestmentAuthorizationService investmentAuthorizationService;

    private final ApplicationEventPublisher eventPublisher;

    private final CustomerLifecycleValidator customerLifecycleValidator;

    @Override
    @Transactional
    public InvestmentResponse createInvestment(
            InvestmentRequest request) {

        validateAmount(request.getAmount());

        Customer customer = customerRepository
                .findById(request.getCustomerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found with id: "
                                        + request.getCustomerId()
                        ));

        customerLifecycleValidator.validateActive(customer);

        if (!investmentAuthorizationService.isCurrentCustomer(
                request.getCustomerId())) {

            throw new BusinessException(
                    "Customer is not authorized to create an investment"
            );
        }

        Account account = accountRepository
                .findByIdForUpdate(request.getAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: "
                                        + request.getAccountId()
                        ));

        validateAccount(customer, account);

        validateMaturityDate(request.getMaturityDate());

        TransactionRequest transactionRequest =
                TransactionRequest.builder()
                        .accountId(account.getId())
                        .amount(request.getAmount())
                        .description(
                                request.getDescription() != null
                                        ? request.getDescription()
                                        : "Investment funding"
                        )
                        .build();

        transactionService.withdraw(transactionRequest);

        Investment investment = Investment.builder()
                .customer(customer)
                .account(account)
                .investmentType(request.getInvestmentType())
                .amount(request.getAmount())
                .startDate(DateTimeUtil.todayUtc())
                .maturityDate(request.getMaturityDate())
                .status(InvestmentStatus.ACTIVE)
                .description(request.getDescription())
                .build();

        Investment savedInvestment =
                investmentRepository.save(investment);

        return investmentMapper.toResponse(
                savedInvestment
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = "investmentById",
            key = "#investmentId"
    )
    public InvestmentResponse getInvestmentById(
            Long investmentId) {

        Investment investment =
                findInvestment(investmentId);

        return investmentMapper.toResponse(
                investment
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentResponse> getInvestmentsByCustomerId(
            Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: "
                            + customerId
            );
        }

        return investmentRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(investmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvestmentResponse> getAllInvestments() {

        return investmentRepository.findAll()
                .stream()
                .map(investmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "investmentById",
            key = "#investmentId"
    )
    public InvestmentResponse matureInvestment(
            Long investmentId) {

        Investment investment =
                investmentRepository
                        .findByIdForUpdate(investmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investment not found with id: "
                                                + investmentId
                                ));

        if (investment.getStatus()
                != InvestmentStatus.ACTIVE) {

            throw new BusinessException(
                    "Only active investments can be matured"
            );
        }

//        if (investment.getMaturityDate()
//                .isAfter(DateTimeUtil.todayUtc())) {
//
//            throw new BusinessException(
//                    "Investment cannot be matured before maturity date"
//            );
//        }

        investment.setStatus(
                InvestmentStatus.MATURED
        );

        eventPublisher.publishEvent(
                new InvestmentMaturityEvent(
                        investment.getCustomer()
                                .getUser()
                                .getId(),
                        investment.getId()
                )
        );

        return investmentMapper.toResponse(
                investment
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "investmentById",
            key = "#investmentId"
    )
    public InvestmentResponse settleInvestment(
            Long investmentId) {


        Investment investment =
                investmentRepository
                        .findByIdForUpdate(investmentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Investment not found with id: "
                                                + investmentId
                                ));

        validateInvestmentForSettlement(
                investment
        );

//        LocalDate today = DateTimeUtil.todayUtc();
//
//        if (investment.getMaturityDate()
//                .isAfter(today)) {
//
//            throw new BusinessException(
//                    "Investment cannot be settled before maturity date"
//            );
//        }

        InvestmentPerformance latestPerformance =
                performanceRepository
                        .findFirstByInvestmentIdOrderByPerformanceDateDesc(
                                investmentId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Cannot settle investment without "
                                                + "a performance record"
                                ));

        Account investmentAccount =
                investment.getAccount();

        if (investmentAccount == null ||
                investmentAccount.getId() == null) {

            throw new BusinessException(
                    "Investment does not have a valid settlement account"
            );
        }

        Account lockedAccount =
                accountRepository
                        .findByIdForUpdate(
                                investmentAccount.getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Settlement account not found: "
                                                + investmentAccount.getId()
                                ));

        if (lockedAccount.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    "Settlement account must be ACTIVE"
            );
        }

        BigDecimal settlementAmount =
                normalizeMoney(
                        latestPerformance.getCurrentValue()
                );

        if (settlementAmount.compareTo(
                BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Settlement amount must be greater than zero"
            );
        }

        TransactionRequest transactionRequest =
                TransactionRequest.builder()
                        .accountId(lockedAccount.getId())
                        .amount(settlementAmount)
                        .description(
                                "Investment maturity settlement - investment "
                                        + investmentId
                        )
                        .build();

        String settlementReference =
                "INV-SETTLE-" + investment.getId();

        transactionService.creditInvestmentSettlement(
                lockedAccount.getId(),
                settlementAmount,
                settlementReference
        );

        investment.setSettlementReference(
                settlementReference
        );

        investment.setSettledAt(
                DateTimeUtil.nowUtc()
        );

        investment.setSettlementAccount(
                lockedAccount
        );

        investment.setStatus(
                InvestmentStatus.CLOSED
        );

        investmentRepository.save(
                investment
        );

        return investmentMapper.toResponse(
                investment
        );
    }

    private Investment findInvestment(
            Long investmentId) {

        return investmentRepository.findById(investmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Investment not found with id: "
                                        + investmentId
                        ));
    }

    private void validateAmount(
            BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Investment amount must be greater than zero"
            );
        }

        if (amount.scale() > MONEY_SCALE) {

            throw new BusinessException(
                    "Investment amount cannot have more than "
                            + MONEY_SCALE
                            + " decimal places"
            );
        }
    }

    private void validateMaturityDate(
            LocalDate maturityDate) {

//        if (maturityDate == null ||
//                !maturityDate.isAfter(DateTimeUtil.todayUtc())) {
//
//            throw new BusinessException(
//                    "Maturity date must be in the future"
//            );
//        }

        if (maturityDate == null ||
                maturityDate.isBefore(DateTimeUtil.todayUtc())) {

            throw new BusinessException(
                    "Maturity date cannot be before today"
            );
        }
    }

    private void validateAccount(
            Customer customer,
            Account account) {

        if (!account.getCustomer()
                .getId()
                .equals(customer.getId())) {

            throw new BusinessException(
                    "Investment account does not belong to the customer"
            );
        }

        if (account.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    "Investment funding is allowed only from an active account"
            );
        }
    }

    private void validateInvestmentForSettlement(
            Investment investment) {

        if (investment.getStatus()
                != InvestmentStatus.MATURED) {

            throw new BusinessException(
                    "Only MATURED investments can be settled"
            );
        }

        if (investment.getMaturityDate() == null) {

            throw new BusinessException(
                    "Investment maturity date is missing"
            );
        }

        if (investment.getAccount() == null) {

            throw new BusinessException(
                    "Investment settlement account is missing"
            );
        }
    }

    private BigDecimal normalizeMoney(
            BigDecimal amount) {

        if (amount == null) {

            throw new BusinessException(
                    "Settlement amount is missing"
            );
        }

        return amount.setScale(
                MONEY_SCALE,
                java.math.RoundingMode.HALF_UP
        );
    }
}