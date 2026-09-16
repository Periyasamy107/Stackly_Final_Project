package com.example.bank.transaction.service;

import com.example.bank.account.entity.Account;
import com.example.bank.account.repository.AccountRepository;
import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.enums.TransactionStatus;
import com.example.bank.common.enums.TransactionType;
import com.example.bank.common.exception.*;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.customer.service.CustomerLifecycleValidator;
import com.example.bank.event.TransactionCompletedEvent;
import com.example.bank.transaction.dto.request.TransactionRequest;
import com.example.bank.transaction.dto.request.TransferRequest;
import com.example.bank.transaction.dto.response.TransactionResponse;
import com.example.bank.transaction.entity.Transaction;
import com.example.bank.transaction.mapper.TransactionMapper;
import com.example.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl
        implements TransactionService {

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final TransactionReferenceGenerator transactionReferenceGenerator;

    private final ApplicationEventPublisher eventPublisher;

    private final CacheManager cacheManager;

    private final CustomerLifecycleValidator customerLifecycleValidator;

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "accountById",
            key = "#request.accountId"
    )
    public TransactionResponse deposit(
            TransactionRequest request) {

        validateAmount(request.getAmount());

        Account account =
                findAccountForUpdate(
                        request.getAccountId()
                );

        validateAccountForTransaction(account);

        account.setBalance(
                account.getBalance()
                        .add(request.getAmount())
        );

        Transaction transaction =
                buildTransaction(
                        account,
                        TransactionType.DEPOSIT,
                        request.getAmount(),
                        request.getDescription(),
                        null
                );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        publishTransactionCompleted(
                savedTransaction
        );

        return transactionMapper.toResponse(
                savedTransaction
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "accountById",
            key = "#request.accountId"
    )
    public TransactionResponse withdraw(
            TransactionRequest request) {

        validateAmount(request.getAmount());

        Account account =
                findAccountForUpdate(
                        request.getAccountId()
                );

        validateAccountForTransaction(account);

        if (account.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient account balance"
            );
        }

        account.setBalance(
                account.getBalance()
                        .subtract(request.getAmount())
        );

        Transaction transaction =
                buildTransaction(
                        account,
                        TransactionType.WITHDRAWAL,
                        request.getAmount(),
                        request.getDescription(),
                        null
                );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        publishTransactionCompleted(
                savedTransaction
        );

        return transactionMapper.toResponse(
                savedTransaction
        );
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "accountById",
                    key = "#request.sourceAccountId"
            ),
            @CacheEvict(
                    cacheNames = "accountById",
                    key = "#request.destinationAccountId"
            )
    })
    public List<TransactionResponse> transfer(
            TransferRequest request) {

        validateTransferRequest(request);

        validateAmount(request.getAmount());

        validateDifferentAccounts(request);

        Account firstLockedAccount;

        Account secondLockedAccount;

        if (request.getSourceAccountId()
                < request.getDestinationAccountId()) {

            firstLockedAccount =
                    findAccountForUpdate(
                            request.getSourceAccountId()
                    );

            secondLockedAccount =
                    findAccountForUpdate(
                            request.getDestinationAccountId()
                    );

        } else {

            firstLockedAccount =
                    findAccountForUpdate(
                            request.getDestinationAccountId()
                    );

            secondLockedAccount =
                    findAccountForUpdate(
                            request.getSourceAccountId()
                    );
        }

        Account sourceAccount;

        Account destinationAccount;

        if (firstLockedAccount.getId()
                .equals(request.getSourceAccountId())) {

            sourceAccount = firstLockedAccount;
            destinationAccount = secondLockedAccount;

        } else {

            destinationAccount = firstLockedAccount;
            sourceAccount = secondLockedAccount;
        }

        validateAccountForTransaction(
                sourceAccount
        );

        validateAccountForTransaction(
                destinationAccount
        );

        if (sourceAccount.getBalance()
                .compareTo(request.getAmount()) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient account balance"
            );
        }

        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(request.getAmount())
        );

        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(request.getAmount())
        );

        Transaction debitTransaction =
                buildTransaction(
                        sourceAccount,
                        TransactionType.TRANSFER_DEBIT,
                        request.getAmount(),
                        request.getDescription(),
                        destinationAccount
                                .getAccountNumber()
                );

        Transaction creditTransaction =
                buildTransaction(
                        destinationAccount,
                        TransactionType.TRANSFER_CREDIT,
                        request.getAmount(),
                        request.getDescription(),
                        sourceAccount
                                .getAccountNumber()
                );

        Transaction savedDebit =
                transactionRepository.save(
                        debitTransaction
                );

        Transaction savedCredit =
                transactionRepository.save(
                        creditTransaction
                );

        publishTransactionCompleted(
                savedDebit
        );

        publishTransactionCompleted(
                savedCredit
        );

        return List.of(
                transactionMapper.toResponse(
                        savedDebit
                ),
                transactionMapper.toResponse(
                        savedCredit
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(
            Long id) {

        Transaction transaction =
                transactionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found with id: "
                                                + id
                                ));

        return transactionMapper.toResponse(
                transaction
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse>
    getAccountTransactions(Long accountId) {

        if (!accountRepository.existsById(accountId)) {

            throw new ResourceNotFoundException(
                    "Account not found with id: "
                            + accountId
            );
        }

        return transactionRepository
                .findByAccountIdOrderByTransactionDateDesc(
                        accountId
                )
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    private Account findAccountForUpdate(
            Long accountId) {

        if (accountId == null) {

            throw new BusinessException(
                    "Account ID is required"
            );
        }

        return accountRepository
                .findByIdForUpdate(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: "
                                        + accountId
                        ));
    }

    private void validateAccountForTransaction(
            Account account) {

        if (account.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    "Account must be ACTIVE for financial transactions"
            );
        }
        customerLifecycleValidator.validateActive(
                account.getCustomer()
        );
    }

    private void validateAmount(
            BigDecimal amount) {

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Transaction amount must be greater than zero"
            );
        }

        if (amount.scale() > 4) {

            throw new BusinessException(
                    "Transaction amount cannot have "
                            + "more than 4 decimal places"
            );
        }
    }

    private void validateTransferRequest(
            TransferRequest request) {

        if (request == null) {

            throw new BusinessException(
                    "Transfer request is required"
            );
        }

        if (request.getSourceAccountId() == null) {

            throw new BusinessException(
                    "Source account ID is required"
            );
        }

        if (request.getDestinationAccountId() == null) {

            throw new BusinessException(
                    "Destination account ID is required"
            );
        }
    }

    private void validateDifferentAccounts(
            TransferRequest request) {

        if (request.getSourceAccountId()
                .equals(
                        request.getDestinationAccountId()
                )) {

            throw new BusinessException(
                    "Source and destination accounts "
                            + "must be different"
            );
        }
    }

    private Transaction buildTransaction(
            Account account,
            TransactionType transactionType,
            BigDecimal amount,
            String description,
            String counterpartyAccount) {

        return Transaction.builder()
                .account(account)
                .transactionReference(
                        transactionReferenceGenerator.generate()
                )
                .transactionType(transactionType)
                .amount(amount)
                .description(description)
                .counterpartyAccount(
                        counterpartyAccount
                )
                .transactionStatus(
                        TransactionStatus.COMPLETED
                )
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "accountById",
            key = "#accountId"
    )
    public void creditLoanDisbursement(
            Long accountId,
            BigDecimal amount,
            String transactionReference,
            String description) {

        if (accountId == null) {
            throw new ValidationException(
                    "Account id is required"
            );
        }

        if (amount == null
                || amount.signum() <= 0) {

            throw new ValidationException(
                    "Disbursement amount must be greater than zero"
            );
        }

        if (transactionReference == null
                || transactionReference.isBlank()) {

            throw new ValidationException(
                    "Transaction reference is required"
            );
        }

        Account account =
                accountRepository.findByIdForUpdate(accountId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account not found with id: "
                                                + accountId
                                )
                        );

        if (account.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    "Loan disbursement account must be ACTIVE"
            );
        }

        customerLifecycleValidator.validateActive(
                account.getCustomer()
        );

        BigDecimal currentBalance =
                account.getBalance() == null
                        ? BigDecimal.ZERO
                        : account.getBalance();

        account.setBalance(
                currentBalance.add(amount)
        );

        accountRepository.save(account);

        Transaction transaction =
                Transaction.builder()
                        .account(account)
                        .transactionReference(
                                transactionReference
                        )
                        .transactionType(
                                TransactionType.LOAN_DISBURSEMENT
                        )
                        .amount(amount)
                        .description(description)
                        .transactionStatus(
                                TransactionStatus.COMPLETED
                        )
                        .build();

        transactionRepository.save(transaction);

        publishTransactionCompleted(
                transaction
        );
    }

    private void publishTransactionCompleted(
            Transaction transaction) {

        if (transaction == null
                || transaction.getAccount() == null
                || transaction.getAccount().getCustomer() == null
                || transaction.getAccount().getCustomer().getUser() == null) {

            return;
        }

        eventPublisher.publishEvent(
                new TransactionCompletedEvent(
                        transaction.getTransactionReference(),
                        transaction.getAccount()
                                .getCustomer()
                                .getUser()
                                .getId(),
                        transaction.getId(),
                        transaction.getTransactionReference(),
                        transaction.getTransactionType(),
                        transaction.getAmount(),
                        transaction.getDescription()
                )
        );
    }

    @Override
    @Transactional
    public TransactionResponse creditInvestmentSettlement(
            Long accountId,
            BigDecimal amount,
            String investmentReference) {

        if (accountId == null) {
            throw new BusinessException(
                    "Settlement account ID is required"
            );
        }

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Investment settlement amount must be greater than zero"
            );
        }

        if (investmentReference == null ||
                investmentReference.isBlank()) {

            throw new BusinessException(
                    "Investment settlement reference is required"
            );
        }

        Account account = accountRepository
                .findByIdForUpdate(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: "
                                        + accountId
                        ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(
                    "Settlement account must be ACTIVE"
            );
        }

        BigDecimal normalizedAmount =
                amount.setScale(
                        4,
                        RoundingMode.HALF_UP
                );

        account.setBalance(
                account.getBalance()
                        .add(normalizedAmount)
        );

        accountRepository.save(account);

        Transaction transaction =
                buildTransaction(
                        account,
                        TransactionType.INVESTMENT_SETTLEMENT,
                        normalizedAmount,
                        "Investment maturity settlement: " + investmentReference,
                        null // investmentReference
                );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        eventPublisher.publishEvent(
                new TransactionCompletedEvent(
                        UUID.randomUUID().toString(),
                        account.getCustomer().getId(),
                        savedTransaction.getId(),
                        savedTransaction.getTransactionReference(),
                        savedTransaction.getTransactionType(),
                        savedTransaction.getAmount(),
                        savedTransaction.getDescription()
                )
        );

        return transactionMapper.toResponse(
                savedTransaction
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "accountById",
            key = "#result.accountId"
    )
    public TransactionResponse reverseTransaction(
            Long transactionId) {

        if (transactionId == null) {

            throw new ValidationException(
                    "Transaction id is required"
            );
        }

        Transaction originalTransaction =
                transactionRepository.findByIdForUpdate(
                                transactionId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Transaction not found with id: "
                                                + transactionId
                                )
                        );


        if (originalTransaction.getTransactionStatus()
                == TransactionStatus.REVERSED) {

            throw new InvalidTransactionException(
                    "Transaction has already been reversed"
            );
        }

        if (originalTransaction.getTransactionStatus()
                != TransactionStatus.COMPLETED) {

            throw new InvalidTransactionException(
                    "Only COMPLETED transactions can be reversed"
            );
        }

        if (originalTransaction.getTransactionType()
                == TransactionType.REVERSED) {

            throw new InvalidTransactionException(
                    "A reversal transaction cannot be reversed"
            );
        }

        Account account =
                accountRepository.findByIdForUpdate(
                                originalTransaction
                                        .getAccount()
                                        .getId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Account not found with id: "
                                                + originalTransaction
                                                .getAccount()
                                                .getId()
                                )
                        );

        if (account.getStatus()
                != AccountStatus.ACTIVE) {

            throw new BusinessException(
                    "Account must be ACTIVE for transaction reversal"
            );
        }

        BigDecimal amount =
                originalTransaction.getAmount();

        if (amount == null ||
                amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidTransactionException(
                    "Transaction amount must be greater than zero"
            );
        }

        if (originalTransaction.getTransactionType()
                == TransactionType.TRANSFER_DEBIT ||
                originalTransaction.getTransactionType()
                        == TransactionType.TRANSFER_CREDIT) {

            throw new InvalidTransactionException(
                    "Individual transfer entries cannot be reversed. "
                            + "The complete transfer must be reversed atomically."
            );
        }

        switch (originalTransaction.getTransactionType()) {

            case DEPOSIT,
                 LOAN_DISBURSEMENT,
                 INVESTMENT_SETTLEMENT -> {

                if (account.getBalance()
                        .compareTo(amount) < 0) {

                    throw new InsufficientBalanceException(
                            "Insufficient account balance "
                                    + "to reverse the transaction"
                    );
                }

                account.setBalance(
                        account.getBalance()
                                .subtract(amount)
                );
            }

            case WITHDRAWAL -> {

                account.setBalance(
                        account.getBalance()
                                .add(amount)
                );
            }

            case TRANSFER_DEBIT,
                 TRANSFER_CREDIT,
                 LOAN_REPAYMENT,
                 REVERSED -> throw new InvalidTransactionException(
                    "Transaction type requires aggregate-level "
                            + "reversal and cannot be individually reversed: "
                            + originalTransaction.getTransactionType()
            );

            default -> throw new InvalidTransactionException(
                    "Transaction type cannot be reversed: "
                            + originalTransaction.getTransactionType()
            );
        }

        accountRepository.save(account);

        var accountCache =
                cacheManager.getCache("accountById");

        if (accountCache != null) {
            accountCache.evict(account.getId());
        }

        Transaction reversalTransaction =
                Transaction.builder()
                        .account(account)
                        .transactionReference(
                                transactionReferenceGenerator
                                        .generate()
                        )
                        .transactionType(
                                TransactionType.REVERSED
                        )
                        .amount(amount)
                        .transactionDate(
                                DateTimeUtil.nowUtc()
                        )
                        .description(
                                "Reversal of transaction "
                                        + originalTransaction
                                        .getTransactionReference()
                        )
                        .counterpartyAccount(
                                originalTransaction
                                        .getCounterpartyAccount()
                        )
                        .transactionStatus(
                                TransactionStatus.COMPLETED
                        )
                        .build();

        Transaction savedReversal =
                transactionRepository.save(
                        reversalTransaction
                );

        originalTransaction.setTransactionStatus(
                TransactionStatus.REVERSED
        );

        transactionRepository.save(
                originalTransaction
        );

        publishTransactionCompleted(
                savedReversal
        );

        return transactionMapper.toResponse(
                savedReversal
        );
    }
}