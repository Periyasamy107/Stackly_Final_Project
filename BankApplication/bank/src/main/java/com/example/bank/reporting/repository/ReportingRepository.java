package com.example.bank.reporting.repository;

import com.example.bank.account.entity.Account;
import com.example.bank.customer.entity.Customer;
import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.TransactionStatus;
import com.example.bank.common.enums.TransactionType;
import com.example.bank.investment.entity.Investment;
import com.example.bank.loan.entity.Loan;
import com.example.bank.reporting.dto.request.ReportRequest;
import com.example.bank.reporting.service.ReportQuery;
import com.example.bank.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;


public interface ReportingRepository {

    Page<Customer> findCustomers(
            ReportQuery query
    );

    Page<Account> findAccounts(
            ReportQuery query
    );

    long countAccountsByCustomer(
            Long customerId
    );

    long countAccountsByCustomerAndStatus(
            Long customerId,
            AccountStatus status
    );

    BigDecimal sumBalanceByCustomer(
            Long customerId
    );

    Page<Customer> findCustomersForReport(
            ReportRequest request,
            Pageable pageable
    );

    Page<Account> findAccountsForReport(
            ReportRequest request,
            Pageable pageable
    );

    Page<Transaction> findTransactionsForReport(
            ReportRequest request,
            Pageable pageable
    );

    BigDecimal sumBalanceByCustomerAndStatus(
            Long customerId,
            AccountStatus status
    );

    long countTransactionsByAccount(
            Long accountId
    );

    long countTransactionsForAccount(Long accountId);

    BigDecimal sumTransactionsByAccountAndType(
            Long accountId,
            TransactionType transactionType,
            TransactionStatus status
    );

    BigDecimal sumTransferAmountByAccount(
            Long accountId,
            TransactionStatus status
    );

    BigDecimal sumDepositsForAccount(Long accountId);

    BigDecimal sumWithdrawalsForAccount(Long accountId);

    BigDecimal sumTransferDebitsForAccount(Long accountId);

    BigDecimal sumTransferCreditsForAccount(Long accountId);

    BigDecimal sumTransferAmountForAccount(Long accountId);

    Page<Loan> findLoansForReport(
            ReportRequest request,
            Pageable pageable
    );

    Page<Investment> findInvestmentsForReport(
            ReportRequest request,
            Pageable pageable
    );

    BigDecimal sumCompletedRepaymentsForLoan(Long loanId);

    long countCompletedRepaymentsForLoan(Long loanId);

    BigDecimal sumInvestmentPerformanceForInvestment(Long investmentId);

    BigDecimal sumInvestmentPerformancePercentageForInvestment(
            Long investmentId
    );

    long countInvestmentPerformanceForInvestment(Long investmentId);
}