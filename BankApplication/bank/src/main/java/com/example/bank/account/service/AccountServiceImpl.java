package com.example.bank.account.service;

import com.example.bank.account.dto.request.AccountRequest;
import com.example.bank.account.dto.response.AccountResponse;
import com.example.bank.account.entity.Account;
import com.example.bank.account.mapper.AccountMapper;
import com.example.bank.account.repository.AccountRepository;
import com.example.bank.common.enums.AccountStatus;
import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.exception.ResourceNotFoundException;
import com.example.bank.customer.entity.Customer;
import com.example.bank.customer.repository.CustomerRepository;
import com.example.bank.customer.service.CustomerLifecycleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;
    private final AccountNumberGenerator accountNumberGenerator;
    private final CustomerLifecycleValidator customerLifecycleValidator;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "accountById",
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = "accountByNumber",
                    allEntries = true
            )
    })
    public AccountResponse createAccount(
            AccountRequest request) {

        Customer customer =
                customerRepository.findById(request.getCustomerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Customer not found with id: "
                                                + request.getCustomerId()
                                ));

        customerLifecycleValidator.validateActive(customer);

        Account account =
                accountMapper.toEntity(request);

        account.setCustomer(customer);
        account.setAccountNumber(
                accountNumberGenerator.generate()
        );
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount =
                accountRepository.save(account);

        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Cacheable(
            cacheNames = "accountById",
            key = "#id"
    )
    public AccountResponse getAccountById(Long id) {

        Account account =
                findAccount(id);

        return accountMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(
            Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException(
                    "Customer not found with id: " + customerId
            );
        }

        return accountRepository
                .findByCustomerId(customerId)
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    public List<AccountResponse> getAllAccounts() {

        return accountRepository.findAll()
                .stream()
                .map(accountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "accountById",
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = "accountByNumber",
                    allEntries = true
            )
    })
    public AccountResponse blockAccount(Long id) {

        Account account = findAccount(id);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(
                    "Closed account cannot be blocked"
            );
        }

        account.setStatus(AccountStatus.BLOCKED);

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "accountById",
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = "accountByNumber",
                    allEntries = true
            )
    })
    public AccountResponse activateAccount(Long id) {

        Account account = findAccount(id);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(
                    "Closed account cannot be activated"
            );
        }

        account.setStatus(AccountStatus.ACTIVE);

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "accountById",
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = "accountByNumber",
                    allEntries = true
            )
    })
    public AccountResponse closeAccount(Long id) {

        Account account = findAccount(id);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(
                    "Account is already closed"
            );
        }

        if (account.getBalance()
                .compareTo(BigDecimal.ZERO) != 0) {

            throw new BusinessException(
                    "Account cannot be closed while balance is not zero"
            );
        }

        account.setStatus(AccountStatus.CLOSED);

        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = "accountById",
                    key = "#id"
            ),
            @CacheEvict(
                    cacheNames = "accountByNumber",
                    allEntries = true
            )
    })
    public AccountResponse deactivateAccount(Long id) {

        Account account = findAccount(id);

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException(
                    "Closed account cannot be deactivated"
            );
        }

        account.setStatus(AccountStatus.INACTIVE);

        return accountMapper.toResponse(account);
    }

    private Account findAccount(Long id) {

        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found with id: " + id
                        ));
    }
}