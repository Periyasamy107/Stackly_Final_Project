package com.example.bank.reporting.repository;

import com.example.bank.account.entity.Account;
import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.TransactionStatus;
import com.example.bank.common.enums.TransactionType;
import com.example.bank.common.enums.InvestmentStatus;
import com.example.bank.common.enums.InvestmentType;
import com.example.bank.common.enums.LoanStatus;
import com.example.bank.common.enums.LoanType;
import com.example.bank.customer.entity.Customer;
import com.example.bank.investment.entity.Investment;
import com.example.bank.loan.entity.Loan;
import com.example.bank.reporting.dto.request.ReportRequest;
import com.example.bank.reporting.service.ReportQuery;
import com.example.bank.transaction.entity.Transaction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReportingRepositoryImpl
        implements ReportingRepository {

    private final EntityManager entityManager;

    @Override
    public Page<Customer> findCustomers(
            ReportQuery query) {

        StringBuilder jpql = new StringBuilder("""
            SELECT c
            FROM Customer c
            LEFT JOIN c.user u
            WHERE 1 = 1
            """);

        StringBuilder countJpql = new StringBuilder("""
            SELECT COUNT(c)
            FROM Customer c
            LEFT JOIN c.user u
            WHERE 1 = 1
            """);

        if (query.customerId() != null) {
            jpql.append(" AND c.id = :customerId");
            countJpql.append(" AND c.id = :customerId");
        }

        appendCustomerDateFilters(
                jpql,
                countJpql,
                query
        );

        jpql.append(" ORDER BY c.createdAt DESC, c.id DESC");

        TypedQuery<Customer> resultQuery =
                entityManager.createQuery(
                        jpql.toString(),
                        Customer.class
                );

        TypedQuery<Long> countQuery =
                entityManager.createQuery(
                        countJpql.toString(),
                        Long.class
                );

        setParameterIfPresent(
                resultQuery,
                countQuery,
                "customerId",
                query.customerId()
        );

        setDateParameters(
                resultQuery,
                countQuery,
                query
        );

        return executePage(
                resultQuery,
                countQuery,
                query.pageable()
        );
    }

    @Override
    public Page<Account> findAccounts(
            ReportQuery query) {

        StringBuilder jpql = new StringBuilder("""
            SELECT a
            FROM Account a
            JOIN FETCH a.customer c
            WHERE 1 = 1
            """);

        StringBuilder countJpql = new StringBuilder("""
            SELECT COUNT(a)
            FROM Account a
            WHERE 1 = 1
            """);

        if (query.customerId() != null) {
            jpql.append(" AND c.id = :customerId");
            countJpql.append(" AND a.customer.id = :customerId");
        }

        if (query.accountId() != null) {
            jpql.append(" AND a.id = :accountId");
            countJpql.append(" AND a.id = :accountId");
        }

        if (query.accountNumber() != null
                && !query.accountNumber().isBlank()) {

            jpql.append(
                    " AND LOWER(a.accountNumber) " +
                            "LIKE LOWER(:accountNumber)"
            );

            countJpql.append(
                    " AND LOWER(a.accountNumber) " +
                            "LIKE LOWER(:accountNumber)"
            );
        }

        appendAccountDateFilters(
                jpql,
                countJpql,
                query
        );

        jpql.append(" ORDER BY a.createdAt DESC, a.id DESC");

        TypedQuery<Account> resultQuery =
                entityManager.createQuery(
                        jpql.toString(),
                        Account.class
                );

        TypedQuery<Long> countQuery =
                entityManager.createQuery(
                        countJpql.toString(),
                        Long.class
                );

        setParameterIfPresent(
                resultQuery,
                countQuery,
                "customerId",
                query.customerId()
        );

        setParameterIfPresent(
                resultQuery,
                countQuery,
                "accountId",
                query.accountId()
        );

        if (query.accountNumber() != null
                && !query.accountNumber().isBlank()) {

            String accountNumber =
                    "%" + query.accountNumber().trim() + "%";

            setParameterIfPresent(
                    resultQuery,
                    countQuery,
                    "accountNumber",
                    accountNumber
            );
        }

        setDateParameters(
                resultQuery,
                countQuery,
                query
        );

        return executePage(
                resultQuery,
                countQuery,
                query.pageable()
        );
    }

    @Override
    public long countAccountsByCustomer(
            Long customerId) {

        return entityManager.createQuery(
                        """
                        SELECT COUNT(a)
                        FROM Account a
                        WHERE a.customer.id = :customerId
                        """,
                        Long.class
                )
                .setParameter(
                        "customerId",
                        customerId
                )
                .getSingleResult();
    }

    @Override
    public long countAccountsByCustomerAndStatus(
            Long customerId,
            AccountStatus status) {

        return entityManager.createQuery(
                        """
                        SELECT COUNT(a)
                        FROM Account a
                        WHERE a.customer.id = :customerId
                          AND a.status = :status
                        """,
                        Long.class
                )
                .setParameter(
                        "customerId",
                        customerId
                )
                .setParameter(
                        "status",
                        status
                )
                .getSingleResult();
    }

    @Override
    public BigDecimal sumBalanceByCustomer(
            Long customerId) {

        BigDecimal result =
                entityManager.createQuery(
                                """
                                SELECT COALESCE(
                                    SUM(a.balance),
                                    0
                                )
                                FROM Account a
                                WHERE a.customer.id = :customerId
                                """,
                                BigDecimal.class
                        )
                        .setParameter(
                                "customerId",
                                customerId
                        )
                        .getSingleResult();

        return nonNullAmount(result);
    }

    @Override
    public BigDecimal sumBalanceByCustomerAndStatus(
            Long customerId,
            AccountStatus status) {

        BigDecimal result =
                entityManager.createQuery(
                                """
                                SELECT COALESCE(
                                    SUM(a.balance),
                                    0
                                )
                                FROM Account a
                                WHERE a.customer.id = :customerId
                                  AND a.status = :status
                                """,
                                BigDecimal.class
                        )
                        .setParameter(
                                "customerId",
                                customerId
                        )
                        .setParameter(
                                "status",
                                status
                        )
                        .getSingleResult();

        return nonNullAmount(result);
    }

    @Override
    public long countTransactionsByAccount(
            Long accountId) {

        return entityManager.createQuery(
                        """
                        SELECT COUNT(t)
                        FROM Transaction t
                        WHERE t.account.id = :accountId
                        """,
                        Long.class
                )
                .setParameter(
                        "accountId",
                        accountId
                )
                .getSingleResult();
    }

    @Override
    public BigDecimal sumTransactionsByAccountAndType(
            Long accountId,
            TransactionType transactionType,
            TransactionStatus status) {

        BigDecimal result =
                entityManager.createQuery(
                                """
                                SELECT COALESCE(
                                    SUM(t.amount),
                                    0
                                )
                                FROM Transaction t
                                WHERE t.account.id = :accountId
                                  AND t.transactionType = :transactionType
                                  AND t.transactionStatus = :status
                                """,
                                BigDecimal.class
                        )
                        .setParameter(
                                "accountId",
                                accountId
                        )
                        .setParameter(
                                "transactionType",
                                transactionType
                        )
                        .setParameter(
                                "status",
                                status
                        )
                        .getSingleResult();

        return nonNullAmount(result);
    }

    @Override
    public BigDecimal sumTransferAmountByAccount(
            Long accountId,
            TransactionStatus status) {

        BigDecimal result =
                entityManager.createQuery(
                                """
                                SELECT COALESCE(
                                    SUM(t.amount),
                                    0
                                )
                                FROM Transaction t
                                WHERE t.account.id = :accountId
                                  AND t.transactionType IN (
                                      com.example.bank.common.enums.TransactionType.TRANSFER_DEBIT,
                                      com.example.bank.common.enums.TransactionType.TRANSFER_CREDIT
                                  )
                                  AND t.transactionStatus = :status
                                """,
                                BigDecimal.class
                        )
                        .setParameter(
                                "accountId",
                                accountId
                        )
                        .setParameter(
                                "status",
                                status
                        )
                        .getSingleResult();

        return nonNullAmount(result);
    }

    @Override
    public Page<Customer> findCustomersForReport(
            ReportRequest request,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT c
                FROM Customer c
                WHERE 1 = 1
                """);

        StringBuilder countJpql = new StringBuilder("""
                SELECT COUNT(c.id)
                FROM Customer c
                WHERE 1 = 1
                """);

        List<ParameterValue> parameters = new ArrayList<>();

        if (request.getCustomerId() != null) {
            jpql.append(" AND c.id = :customerId");
            countJpql.append(" AND c.id = :customerId");
            parameters.add(new ParameterValue("customerId", request.getCustomerId()));
        }

        if (StringUtils.hasText(String.valueOf(request.getFromDate()))) {
            LocalDateTime from = request.getFromDate().atStartOfDay();

            jpql.append(" AND c.createdAt >= :fromDate");
            countJpql.append(" AND c.createdAt >= :fromDate");

            parameters.add(new ParameterValue("fromDate", from));
        }

        if (request.getToDate() != null) {
            LocalDateTime to = request.getToDate()
                    .plusDays(1)
                    .atStartOfDay();

            jpql.append(" AND c.createdAt < :toDate");
            countJpql.append(" AND c.createdAt < :toDate");

            parameters.add(new ParameterValue("toDate", to));
        }

        jpql.append(" ORDER BY c.id");

        TypedQuery<Customer> query =
                entityManager.createQuery(jpql.toString(), Customer.class);

        TypedQuery<Long> countQuery =
                entityManager.createQuery(countJpql.toString(), Long.class);

        applyParameters(query, parameters);
        applyParameters(countQuery, parameters);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countQuery.getSingleResult()
        );
    }

    @Override
    public Page<Account> findAccountsForReport(
            ReportRequest request,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT a
                FROM Account a
                JOIN FETCH a.customer c
                WHERE 1 = 1
                """);

        StringBuilder countJpql = new StringBuilder("""
                SELECT COUNT(a.id)
                FROM Account a
                WHERE 1 = 1
                """);

        List<ParameterValue> parameters = new ArrayList<>();

        if (request.getCustomerId() != null) {
            jpql.append(" AND c.id = :customerId");
            countJpql.append(" AND a.customer.id = :customerId");

            parameters.add(
                    new ParameterValue("customerId", request.getCustomerId())
            );
        }

        if (request.getAccountId() != null) {
            jpql.append(" AND a.id = :accountId");
            countJpql.append(" AND a.id = :accountId");

            parameters.add(
                    new ParameterValue("accountId", request.getAccountId())
            );
        }

        if (StringUtils.hasText(request.getAccountNumber())) {
            jpql.append(" AND LOWER(a.accountNumber) LIKE LOWER(:accountNumber)");
            countJpql.append(
                    " AND LOWER(a.accountNumber) LIKE LOWER(:accountNumber)"
            );

            parameters.add(
                    new ParameterValue(
                            "accountNumber",
                            "%" + request.getAccountNumber().trim() + "%"
                    )
            );
        }

        jpql.append(" ORDER BY a.id");

        TypedQuery<Account> query =
                entityManager.createQuery(jpql.toString(), Account.class);

        TypedQuery<Long> countQuery =
                entityManager.createQuery(countJpql.toString(), Long.class);

        applyParameters(query, parameters);
        applyParameters(countQuery, parameters);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countQuery.getSingleResult()
        );
    }

    @Override
    public Page<Transaction> findTransactionsForReport(
            ReportRequest request,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT t
                FROM Transaction t
                JOIN FETCH t.account a
                JOIN FETCH a.customer c
                WHERE 1 = 1
                """);

        StringBuilder countJpql = new StringBuilder("""
                SELECT COUNT(t.id)
                FROM Transaction t
                JOIN t.account a
                JOIN a.customer c
                WHERE 1 = 1
                """);

        List<ParameterValue> parameters = new ArrayList<>();

        if (request.getCustomerId() != null) {
            jpql.append(" AND c.id = :customerId");
            countJpql.append(" AND c.id = :customerId");

            parameters.add(
                    new ParameterValue("customerId", request.getCustomerId())
            );
        }

        if (request.getAccountId() != null) {
            jpql.append(" AND a.id = :accountId");
            countJpql.append(" AND a.id = :accountId");

            parameters.add(
                    new ParameterValue("accountId", request.getAccountId())
            );
        }

        if (StringUtils.hasText(request.getAccountNumber())) {
            jpql.append(" AND a.accountNumber = :accountNumber");
            countJpql.append(" AND a.accountNumber = :accountNumber");

            parameters.add(
                    new ParameterValue(
                            "accountNumber",
                            request.getAccountNumber().trim()
                    )
            );
        }

        if (StringUtils.hasText(request.getTransactionType())) {

            TransactionType transactionType =
                    parseEnum(
                            TransactionType.class,
                            request.getTransactionType(),
                            "transactionType"
                    );

            jpql.append(" AND t.transactionType = :transactionType");
            countJpql.append(" AND t.transactionType = :transactionType");

            parameters.add(
                    new ParameterValue(
                            "transactionType",
                            transactionType
                    )
            );
        }

        if (StringUtils.hasText(request.getTransactionStatus())) {

            TransactionStatus transactionStatus =
                    parseEnum(
                            TransactionStatus.class,
                            request.getTransactionStatus(),
                            "transactionStatus"
                    );

            jpql.append(" AND t.transactionStatus = :transactionStatus");
            countJpql.append(" AND t.transactionStatus = :transactionStatus");

            parameters.add(
                    new ParameterValue(
                            "transactionStatus",
                            transactionStatus
                    )
            );
        }

        if (request.getFromDate() != null) {
            LocalDateTime fromDate =
                    request.getFromDate().atStartOfDay();

            jpql.append(" AND t.transactionDate >= :fromDate");
            countJpql.append(" AND t.transactionDate >= :fromDate");

            parameters.add(
                    new ParameterValue("fromDate", fromDate)
            );
        }

        if (request.getToDate() != null) {
            LocalDateTime toDate =
                    request.getToDate()
                            .plusDays(1)
                            .atStartOfDay();

            jpql.append(" AND t.transactionDate < :toDate");
            countJpql.append(" AND t.transactionDate < :toDate");

            parameters.add(
                    new ParameterValue("toDate", toDate)
            );
        }

        if (request.getMinAmount() != null) {
            jpql.append(" AND t.amount >= :minAmount");
            countJpql.append(" AND t.amount >= :minAmount");

            parameters.add(
                    new ParameterValue("minAmount", request.getMinAmount())
            );
        }

        if (request.getMaxAmount() != null) {
            jpql.append(" AND t.amount <= :maxAmount");
            countJpql.append(" AND t.amount <= :maxAmount");

            parameters.add(
                    new ParameterValue("maxAmount", request.getMaxAmount())
            );
        }

        jpql.append(" ORDER BY t.transactionDate DESC, t.id DESC");

        TypedQuery<Transaction> query =
                entityManager.createQuery(jpql.toString(), Transaction.class);

        TypedQuery<Long> countQuery =
                entityManager.createQuery(countJpql.toString(), Long.class);

        applyParameters(query, parameters);
        applyParameters(countQuery, parameters);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countQuery.getSingleResult()
        );
    }

    @Override
    public long countTransactionsForAccount(Long accountId) {
        return entityManager.createQuery("""
                SELECT COUNT(t.id)
                FROM Transaction t
                WHERE t.account.id = :accountId
                """, Long.class)
                .setParameter("accountId", accountId)
                .getSingleResult();
    }

    @Override
    public BigDecimal sumDepositsForAccount(Long accountId) {
        return getAmountSum(
                accountId,
                "DEPOSIT"
        );
    }

    @Override
    public BigDecimal sumWithdrawalsForAccount(Long accountId) {
        return getAmountSum(
                accountId,
                "WITHDRAWAL"
        );
    }

    @Override
    public BigDecimal sumTransferDebitsForAccount(Long accountId) {
        return getAmountSum(
                accountId,
                "TRANSFER_DEBIT"
        );
    }

    @Override
    public BigDecimal sumTransferCreditsForAccount(Long accountId) {
        return getAmountSum(
                accountId,
                "TRANSFER_CREDIT"
        );
    }

    @Override
    public BigDecimal sumTransferAmountForAccount(Long accountId) {
        return getAmountSum(
                accountId,
                "TRANSFER_DEBIT"
        );
    }

    @Override
    public Page<Loan> findLoansForReport(
            ReportRequest request,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("""
            SELECT l
            FROM Loan l
            JOIN FETCH l.customer c
            WHERE 1 = 1
            """);

        StringBuilder countJpql = new StringBuilder("""
            SELECT COUNT(l.id)
            FROM Loan l
            WHERE 1 = 1
            """);

        List<ParameterValue> parameters = new ArrayList<>();

        if (request.getCustomerId() != null) {
            jpql.append(" AND c.id = :customerId");
            countJpql.append(" AND l.customer.id = :customerId");

            parameters.add(
                    new ParameterValue(
                            "customerId",
                            request.getCustomerId()
                    )
            );
        }

        if (request.getLoanId() != null) {
            jpql.append(" AND l.id = :loanId");
            countJpql.append(" AND l.id = :loanId");

            parameters.add(
                    new ParameterValue(
                            "loanId",
                            request.getLoanId()
                    )
            );
        }

        if (StringUtils.hasText(request.getLoanType())) {

            LoanType loanType =
                    parseEnum(
                            LoanType.class,
                            request.getLoanType(),
                            "loanType"
                    );

            jpql.append(" AND l.loanType = :loanType");
            countJpql.append(" AND l.loanType = :loanType");

            parameters.add(
                    new ParameterValue(
                            "loanType",
                            loanType
                    )
            );
        }

        if (StringUtils.hasText(request.getLoanStatus())) {

            LoanStatus loanStatus =
                    parseEnum(
                            LoanStatus.class,
                            request.getLoanStatus(),
                            "loanStatus"
                    );

            jpql.append(" AND l.status = :loanStatus");
            countJpql.append(" AND l.status = :loanStatus");

            parameters.add(
                    new ParameterValue(
                            "loanStatus",
                            loanStatus
                    )
            );
        }

        if (request.getFromDate() != null) {
            LocalDateTime fromDate =
                    request.getFromDate().atStartOfDay();

            jpql.append(" AND l.startDate >= :fromDate");
            countJpql.append(" AND l.startDate >= :fromDate");

            parameters.add(
                    new ParameterValue(
                            "fromDate",
                            fromDate.toLocalDate()
                    )
            );
        }

        if (request.getToDate() != null) {
            LocalDateTime toDate =
                    request.getToDate()
                            .plusDays(1)
                            .atStartOfDay();

            jpql.append(" AND l.startDate < :toDate");
            countJpql.append(" AND l.startDate < :toDate");

            parameters.add(
                    new ParameterValue(
                            "toDate",
                            toDate.toLocalDate()
                    )
            );
        }

        jpql.append(" ORDER BY l.startDate DESC, l.id DESC");

        TypedQuery<Loan> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Loan.class
                );

        TypedQuery<Long> countQuery =
                entityManager.createQuery(
                        countJpql.toString(),
                        Long.class
                );

        applyParameters(query, parameters);
        applyParameters(countQuery, parameters);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countQuery.getSingleResult()
        );
    }

    @Override
    public BigDecimal sumCompletedRepaymentsForLoan(Long loanId) {
        BigDecimal result = entityManager.createQuery("""
            SELECT COALESCE(SUM(r.amount), 0)
            FROM LoanRepayment r
            WHERE r.loan.id = :loanId
              AND r.status = 'COMPLETED'
            """, BigDecimal.class)
                .setParameter("loanId", loanId)
                .getSingleResult();

        return result == null
                ? BigDecimal.ZERO
                : result;
    }

    @Override
    public long countCompletedRepaymentsForLoan(Long loanId) {
        return entityManager.createQuery("""
            SELECT COUNT(r.id)
            FROM LoanRepayment r
            WHERE r.loan.id = :loanId
              AND r.status = 'COMPLETED'
            """, Long.class)
                .setParameter("loanId", loanId)
                .getSingleResult();
    }

    @Override
    public Page<Investment> findInvestmentsForReport(
            ReportRequest request,
            Pageable pageable
    ) {
        StringBuilder jpql = new StringBuilder("""
            SELECT i
            FROM Investment i
            JOIN FETCH i.customer c
            JOIN FETCH i.account a
            WHERE 1 = 1
            """);

        StringBuilder countJpql = new StringBuilder("""
            SELECT COUNT(i.id)
            FROM Investment i
            WHERE 1 = 1
            """);

        List<ParameterValue> parameters = new ArrayList<>();

        if (request.getCustomerId() != null) {
            jpql.append(" AND c.id = :customerId");
            countJpql.append(" AND i.customer.id = :customerId");

            parameters.add(
                    new ParameterValue(
                            "customerId",
                            request.getCustomerId()
                    )
            );
        }

        if (request.getInvestmentId() != null) {
            jpql.append(" AND i.id = :investmentId");
            countJpql.append(" AND i.id = :investmentId");

            parameters.add(
                    new ParameterValue(
                            "investmentId",
                            request.getInvestmentId()
                    )
            );
        }

        if (request.getAccountId() != null) {
            jpql.append(" AND a.id = :accountId");
            countJpql.append(" AND i.account.id = :accountId");

            parameters.add(
                    new ParameterValue(
                            "accountId",
                            request.getAccountId()
                    )
            );
        }

        if (StringUtils.hasText(request.getInvestmentType())) {

            InvestmentType investmentType =
                    parseEnum(
                            InvestmentType.class,
                            request.getInvestmentType(),
                            "investmentType"
                    );

            jpql.append(" AND i.investmentType = :investmentType");
            countJpql.append(" AND i.investmentType = :investmentType");

            parameters.add(
                    new ParameterValue(
                            "investmentType",
                            investmentType
                    )
            );
        }

        if (StringUtils.hasText(request.getInvestmentStatus())) {

            InvestmentStatus investmentStatus =
                    parseEnum(
                            InvestmentStatus.class,
                            request.getInvestmentStatus(),
                            "investmentStatus"
                    );

            jpql.append(" AND i.status = :investmentStatus");
            countJpql.append(" AND i.status = :investmentStatus");

            parameters.add(
                    new ParameterValue(
                            "investmentStatus",
                            investmentStatus
                    )
            );
        }

        if (request.getFromDate() != null) {
            jpql.append(" AND i.startDate >= :fromDate");
            countJpql.append(" AND i.startDate >= :fromDate");

            parameters.add(
                    new ParameterValue(
                            "fromDate",
                            request.getFromDate()
                    )
            );
        }

        if (request.getToDate() != null) {
            jpql.append(" AND i.startDate < :toDate");
            countJpql.append(" AND i.startDate < :toDate");

            parameters.add(
                    new ParameterValue(
                            "toDate",
                            request.getToDate().plusDays(1)
                    )
            );
        }

        jpql.append(" ORDER BY i.startDate DESC, i.id DESC");

        TypedQuery<Investment> query =
                entityManager.createQuery(
                        jpql.toString(),
                        Investment.class
                );

        TypedQuery<Long> countQuery =
                entityManager.createQuery(
                        countJpql.toString(),
                        Long.class
                );

        applyParameters(query, parameters);
        applyParameters(countQuery, parameters);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(
                query.getResultList(),
                pageable,
                countQuery.getSingleResult()
        );
    }

    @Override
    public BigDecimal sumInvestmentPerformanceForInvestment(
            Long investmentId) {

        BigDecimal result = entityManager.createQuery(
                        """
                        SELECT COALESCE(SUM(p.profitLossAmount), 0)
                        FROM InvestmentPerformance p
                        WHERE p.investment.id = :investmentId
                        """,
                        BigDecimal.class
                )
                .setParameter("investmentId", investmentId)
                .getSingleResult();

        return result == null
                ? BigDecimal.ZERO
                : result;
    }

    @Override
    public BigDecimal sumInvestmentPerformancePercentageForInvestment(
            Long investmentId) {

        BigDecimal result = entityManager.createQuery(
                        """
                        SELECT COALESCE(AVG(p.returnPercentage), 0)
                        FROM InvestmentPerformance p
                        WHERE p.investment.id = :investmentId
                        """,
                        BigDecimal.class
                )
                .setParameter("investmentId", investmentId)
                .getSingleResult();

        return result == null
                ? BigDecimal.ZERO
                : result;
    }

    @Override
    public long countInvestmentPerformanceForInvestment(
            Long investmentId) {

        return entityManager.createQuery(
                        """
                        SELECT COUNT(p.id)
                        FROM InvestmentPerformance p
                        WHERE p.investment.id = :investmentId
                        """,
                        Long.class
                )
                .setParameter("investmentId", investmentId)
                .getSingleResult();
    }

    private <E extends Enum<E>> E parseEnum(
            Class<E> enumType,
            String value,
            String fieldName) {

        try {
            return Enum.valueOf(
                    enumType,
                    value.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid " + fieldName + ": " + value,
                    exception
            );
        }
    }

    private BigDecimal getAmountSum(
            Long accountId,
            String transactionType) {

        TransactionType type =
                TransactionType.valueOf(transactionType);

        BigDecimal result =
                entityManager.createQuery(
                                """
                                SELECT COALESCE(
                                    SUM(t.amount),
                                    0
                                )
                                FROM Transaction t
                                WHERE t.account.id = :accountId
                                  AND t.transactionType = :transactionType
                                  AND t.transactionStatus = :status
                                """,
                                BigDecimal.class
                        )
                        .setParameter(
                                "accountId",
                                accountId
                        )
                        .setParameter(
                                "transactionType",
                                type
                        )
                        .setParameter(
                                "status",
                                TransactionStatus.COMPLETED
                        )
                        .getSingleResult();

        return nonNullAmount(result);
    }

    private void applyParameters(
            TypedQuery<?> query,
            List<ParameterValue> parameters
    ) {
        parameters.forEach(
                parameter ->
                        query.setParameter(
                                parameter.name(),
                                parameter.value()
                        )
        );
    }

    private record ParameterValue(
            String name,
            Object value
    ) {
    }


    private void appendCustomerDateFilters(
            StringBuilder jpql,
            StringBuilder countJpql,
            ReportQuery query) {

        LocalDateTime fromDate =
                query.fromDate() == null
                        ? null
                        : query.fromDate()
                        .atStartOfDay();

        LocalDateTime toDate =
                query.toDate() == null
                        ? null
                        : query.toDate()
                        .plusDays(1)
                        .atStartOfDay();

        appendDateFilter(
                jpql,
                countJpql,
                "c.createdAt",
                fromDate,
                toDate
        );
    }

    private void appendAccountDateFilters(
            StringBuilder jpql,
            StringBuilder countJpql,
            ReportQuery query) {

        LocalDateTime fromDate =
                query.fromDate() == null
                        ? null
                        : query.fromDate()
                        .atStartOfDay();

        LocalDateTime toDate =
                query.toDate() == null
                        ? null
                        : query.toDate()
                        .plusDays(1)
                        .atStartOfDay();

        appendDateFilter(
                jpql,
                countJpql,
                "a.createdAt",
                fromDate,
                toDate
        );
    }

    private void appendDateFilter(
            StringBuilder jpql,
            StringBuilder countJpql,
            String property,
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        if (fromDate != null) {

            jpql.append(
                    " AND "
                            + property
                            + " >= :fromDate"
            );

            countJpql.append(
                    " AND "
                            + property
                            + " >= :fromDate"
            );
        }

        if (toDate != null) {

            jpql.append(
                    " AND "
                            + property
                            + " < :toDate"
            );

            countJpql.append(
                    " AND "
                            + property
                            + " < :toDate"
            );
        }
    }

    private void setDateParameters(
            TypedQuery<?> resultQuery,
            TypedQuery<?> countQuery,
            ReportQuery query) {

        if (query.fromDate() != null) {

            resultQuery.setParameter(
                    "fromDate",
                    query.fromDate()
                            .atStartOfDay()
            );

            countQuery.setParameter(
                    "fromDate",
                    query.fromDate()
                            .atStartOfDay()
            );
        }

        if (query.toDate() != null) {

            LocalDateTime exclusiveEnd =
                    query.toDate()
                            .plusDays(1)
                            .atStartOfDay();

            resultQuery.setParameter(
                    "toDate",
                    exclusiveEnd
            );

            countQuery.setParameter(
                    "toDate",
                    exclusiveEnd
            );
        }
    }

    private void setParameterIfPresent(
            TypedQuery<?> resultQuery,
            TypedQuery<?> countQuery,
            String parameter,
            Object value) {

        if (value != null) {

            resultQuery.setParameter(
                    parameter,
                    value
            );

            countQuery.setParameter(
                    parameter,
                    value
            );
        }
    }

    private <T> Page<T> executePage(
            TypedQuery<T> resultQuery,
            TypedQuery<Long> countQuery,
            Pageable pageable) {

        resultQuery.setFirstResult(
                (int) pageable.getOffset()
        );

        resultQuery.setMaxResults(
                pageable.getPageSize()
        );

        List<T> content =
                resultQuery.getResultList();

        long total =
                countQuery.getSingleResult();

        return new PageImpl<>(
                content,
                pageable,
                total
        );
    }

    private BigDecimal nonNullAmount(
            BigDecimal value) {

        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}