package com.example.bank.reporting.service;

import com.example.bank.account.entity.Account;
import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.TransactionStatus;
import com.example.bank.common.enums.TransactionType;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.util.PageableUtil;
import com.example.bank.customer.entity.Customer;
import com.example.bank.investment.entity.Investment;
import com.example.bank.loan.entity.Loan;
import com.example.bank.reporting.dto.request.ReportRequest;
import com.example.bank.reporting.dto.response.*;
import com.example.bank.reporting.enums.ReportType;
import com.example.bank.reporting.repository.ReportingRepository;
import com.example.bank.transaction.entity.Transaction;
import com.example.bank.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportingServiceImpl
        implements ReportingService {

    private final ReportingRepository reportingRepository;

    @Override
    public ReportQuery prepareQuery(ReportRequest request) {

        if (request == null) {

            throw new BusinessException(
                    "Report request is required"
            );
        }

        if (request.getReportType() == null) {

            throw new BusinessException(
                    "Report type is required"
            );
        }

        validateIds(request);
        validateDateRange(request);
        validateAmountRange(request);

        Pageable pageable = createPageable(request);

        return new ReportQuery(
                request.getReportType(),
                request.getCustomerId(),
                request.getAccountId(),
                request.getAccountNumber(),
                request.getTransactionType(),
                request.getTransactionStatus(),
                request.getFromDate(),
                request.getToDate(),
                request.getMinAmount(),
                request.getMaxAmount(),
                request.getSortBy(),
                request.getDirection(),
                pageable
        );
    }


    @Override
    public Pageable createPageable(
            ReportRequest request) {

        if (request == null) {

            throw new BusinessException(
                    "Report request is required"
            );
        }

        return com.example.bank.common.util.PageableUtil.create(
                request.getPage(),
                request.getSize(),
                request.getSortBy(),
                request.getDirection()
        );
    }

    @Override
    public Page<CustomerReportResponse> customerReport(
            ReportRequest request) {

        ReportQuery query =
                prepareQuery(request);

        if (query.reportType()
                != ReportType.CUSTOMER) {

            throw new BusinessException(
                    "Report type must be CUSTOMER"
            );
        }

        return reportingRepository
                .findCustomers(query)
                .map(this::toCustomerReport);
    }

    @Override
    public Page<AccountReportResponse> accountReport(
            ReportRequest request) {

        ReportQuery query =
                prepareQuery(request);

        if (query.reportType()
                != ReportType.ACCOUNT) {

            throw new BusinessException(
                    "Report type must be ACCOUNT"
            );
        }

        return reportingRepository
                .findAccounts(query)
                .map(this::toAccountReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionReportResponse> transactionReport(
            ReportRequest request
    ) {

        validateReportRequest(
                request,
                ReportType.TRANSACTION
        );

        Pageable pageable =
                createPageable(request);

        return reportingRepository
                .findTransactionsForReport(
                        request,
                        pageable
                )
                .map(this::mapTransactionReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoanReportResponse> loanReport(
            ReportRequest request
    ) {

        validateReportRequest(
                request,
                ReportType.LOAN
        );

        Pageable pageable =
                createPageable(request);

        return reportingRepository
                .findLoansForReport(
                        request,
                        pageable
                )
                .map(this::mapLoanReport);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvestmentReportResponse> investmentReport(
            ReportRequest request
    ) {

        validateReportRequest(
                request,
                ReportType.INVESTMENT
        );

        Pageable pageable =
                createPageable(request);

        return reportingRepository
                .findInvestmentsForReport(
                        request,
                        pageable
                )
                .map(this::mapInvestmentReport);
    }

    private void validateReportRequest(
            ReportRequest request,
            ReportType expectedReportType) {

        if (request == null) {

            throw new BusinessException(
                    "Report request is required"
            );
        }

        if (expectedReportType == null) {

            throw new BusinessException(
                    "Expected report type is required"
            );
        }

        validateIds(request);
        validateDateRange(request);
        validateAmountRange(request);
    }

    private void validateAmountRange(
            ReportRequest request) {

        if (request.getMinAmount() != null
                && request.getMaxAmount() != null
                && request.getMinAmount()
                .compareTo(request.getMaxAmount()) > 0) {

            throw new BusinessException(
                    "minAmount must be less than or equal to maxAmount"
            );
        }
    }

    private InvestmentReportResponse mapInvestmentReport(
            Investment investment
    ) {
        BigDecimal performanceAmount =
                reportingRepository
                        .sumInvestmentPerformanceForInvestment(
                                investment.getId()
                        );

        if (performanceAmount == null) {
            performanceAmount = BigDecimal.ZERO;
        }

        BigDecimal currentValue =
                investment.getAmount()
                        .add(performanceAmount);

        BigDecimal performancePercentage =
                reportingRepository
                        .sumInvestmentPerformancePercentageForInvestment(
                                investment.getId()
                        );

        long performanceRecordCount =
                reportingRepository
                        .countInvestmentPerformanceForInvestment(
                                investment.getId()
                        );

        Customer customer = investment.getCustomer();

        Account account = investment.getAccount();

        return InvestmentReportResponse.builder()
                .investmentId(investment.getId())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .investmentType(
                        investment.getInvestmentType() == null
                                ? null
                                : investment.getInvestmentType().name()
                )
                .amount(investment.getAmount())
                .startDate(investment.getStartDate())
                .maturityDate(investment.getMaturityDate())
                .status(
                        investment.getStatus() == null
                                ? null
                                : investment.getStatus().name()
                )
                .currentValue(
                        currentValue
                )
                .totalPerformanceAmount(
                        performanceAmount
                )
                .totalPerformancePercentage(
                        performancePercentage
                )
                .performanceRecordCount(
                        performanceRecordCount
                )
                .build();
    }

    private LoanReportResponse mapLoanReport(Loan loan) {

        BigDecimal completedRepaymentAmount =
                reportingRepository.sumCompletedRepaymentsForLoan(
                        loan.getId()
                );

        long repaymentCount =
                reportingRepository.countCompletedRepaymentsForLoan(
                        loan.getId()
                );

        BigDecimal outstandingAmount =
                loan.getPrincipalAmount()
                        .subtract(completedRepaymentAmount);

        if (outstandingAmount.signum() < 0) {
            outstandingAmount = BigDecimal.ZERO;
        }

        Customer customer = loan.getCustomer();

        return LoanReportResponse.builder()
                .loanId(loan.getId())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .loanType(
                        loan.getLoanType() == null
                                ? null
                                : loan.getLoanType().name()
                )
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .status(
                        loan.getStatus() == null
                                ? null
                                : loan.getStatus().name()
                )
                .totalRepaidAmount(completedRepaymentAmount)
                .outstandingAmount(outstandingAmount)
                .repaymentCount(repaymentCount)
                .completedRepaymentAmount(completedRepaymentAmount)
                .build();
    }

    private TransactionReportResponse mapTransactionReport(
            Transaction transaction
    ) {
        Account account = transaction.getAccount();
        Customer customer = account.getCustomer();

        return TransactionReportResponse.builder()
                .transactionId(transaction.getId())
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .transactionType(
                        transaction.getTransactionType() == null
                                ? null
                                : transaction.getTransactionType().name()
                )
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .transactionReference(transaction.getTransactionReference())
                .counterpartyAccount(transaction.getCounterpartyAccount())
                .transactionStatus(
                        transaction.getTransactionStatus() == null
                                ? null
                                : transaction.getTransactionStatus().name()
                )
                .build();
    }

    private CustomerReportResponse toCustomerReport(
            Customer customer) {

        User user =
                customer.getUser();

        long totalAccounts =
                reportingRepository
                        .countAccountsByCustomer(
                                customer.getId()
                        );

        long activeAccounts =
                reportingRepository
                        .countAccountsByCustomerAndStatus(
                                customer.getId(),
                                AccountStatus.ACTIVE
                        );

        long inactiveAccounts =
                reportingRepository
                        .countAccountsByCustomerAndStatus(
                                customer.getId(),
                                AccountStatus.INACTIVE
                        );

        long blockedAccounts =
                reportingRepository
                        .countAccountsByCustomerAndStatus(
                                customer.getId(),
                                AccountStatus.BLOCKED
                        );

        long closedAccounts =
                reportingRepository
                        .countAccountsByCustomerAndStatus(
                                customer.getId(),
                                AccountStatus.CLOSED
                        );

        BigDecimal totalBalance =
                reportingRepository
                        .sumBalanceByCustomer(
                                customer.getId()
                        );

        BigDecimal activeBalance =
                reportingRepository
                        .sumBalanceByCustomerAndStatus(
                                customer.getId(),
                                AccountStatus.ACTIVE
                        );

        return CustomerReportResponse.builder()
                .customerId(
                        customer.getId()
                )
                .userId(
                        user == null
                                ? null
                                : user.getId()
                )
                .username(
                        user == null
                                ? null
                                : user.getUsername()
                )
                .name(
                        customer.getName()
                )
                .email(
                        customer.getEmail()
                )
                .phone(
                        customer.getPhone()
                )
                .address(
                        customer.getAddress()
                )
                .status(
                        customer.getStatus()
                )
                .createdAt(
                        customer.getCreatedAt()
                )
                .updatedAt(
                        customer.getUpdatedAt()
                )
                .totalAccounts(
                        Math.toIntExact(
                                totalAccounts
                        )
                )
                .activeAccounts(
                        Math.toIntExact(
                                activeAccounts
                        )
                )
                .inactiveAccounts(
                        Math.toIntExact(
                                inactiveAccounts
                        )
                )
                .blockedAccounts(
                        Math.toIntExact(
                                blockedAccounts
                        )
                )
                .closedAccounts(
                        Math.toIntExact(
                                closedAccounts
                        )
                )
                .totalBalance(
                        totalBalance
                )
                .activeBalance(
                        activeBalance
                )
                .build();
    }

    private AccountReportResponse toAccountReport(
            Account account) {

        Customer customer =
                account.getCustomer();

        long transactionCount =
                reportingRepository
                        .countTransactionsByAccount(
                                account.getId()
                        );

        BigDecimal deposits =
                reportingRepository
                        .sumTransactionsByAccountAndType(
                                account.getId(),
                                TransactionType.DEPOSIT,
                                TransactionStatus.COMPLETED
                        );

        BigDecimal withdrawals =
                reportingRepository
                        .sumTransactionsByAccountAndType(
                                account.getId(),
                                TransactionType.WITHDRAWAL,
                                TransactionStatus.COMPLETED
                        );

        BigDecimal transferDebits =
                reportingRepository
                        .sumTransactionsByAccountAndType(
                                account.getId(),
                                TransactionType.TRANSFER_DEBIT,
                                TransactionStatus.COMPLETED
                        );

        BigDecimal transferCredits =
                reportingRepository
                        .sumTransactionsByAccountAndType(
                                account.getId(),
                                TransactionType.TRANSFER_CREDIT,
                                TransactionStatus.COMPLETED
                        );

        BigDecimal totalTransferAmount =
                transferDebits.add(
                        transferCredits
                );

        return AccountReportResponse.builder()
                .accountId(
                        account.getId()
                )
                .accountNumber(
                        account.getAccountNumber()
                )
                .accountType(
                        account.getAccountType()
                )
                .status(
                        account.getStatus()
                )
                .balance(
                        account.getBalance()
                )
                .customerId(
                        customer == null
                                ? null
                                : customer.getId()
                )
                .customerName(
                        customer == null
                                ? null
                                : customer.getName()
                )
                .customerEmail(
                        customer == null
                                ? null
                                : customer.getEmail()
                )
                .createdAt(
                        account.getCreatedAt()
                )
                .updatedAt(
                        account.getUpdatedAt()
                )
                .transactionCount(
                        transactionCount
                )
                .totalDeposits(
                        deposits
                )
                .totalWithdrawals(
                        withdrawals
                )
                .totalTransferDebits(
                        transferDebits
                )
                .totalTransferCredits(
                        transferCredits
                )
                .totalTransferAmount(
                        totalTransferAmount
                )
                .build();
    }

    private void validateIds(
            ReportRequest request) {

        validatePositive(
                request.getCustomerId(),
                "customerId"
        );

        validatePositive(
                request.getAccountId(),
                "accountId"
        );

        validatePositive(
                request.getLoanId(),
                "loanId"
        );

        validatePositive(
                request.getInvestmentId(),
                "investmentId"
        );
    }

    private void validatePositive(
            Long value,
            String field) {

        if (value != null
                && value <= 0) {

            throw new BusinessException(
                    field
                            + " must be greater than zero"
            );
        }
    }

    private void validateDateRange(
            ReportRequest request) {

        if (request.getFromDate() != null
                && request.getToDate() != null
                && request.getFromDate()
                .isAfter(request.getToDate())) {

            throw new BusinessException(
                    "fromDate must be before or equal to toDate"
            );
        }
    }

    private String normalizeSearch(
            String search) {

        if (search == null
                || search.isBlank()) {

            return null;
        }

        return search.trim();
    }
}