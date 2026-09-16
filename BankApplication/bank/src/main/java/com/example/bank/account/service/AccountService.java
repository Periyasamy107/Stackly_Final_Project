package com.example.bank.account.service;

import com.example.bank.account.dto.request.AccountRequest;
import com.example.bank.account.dto.response.AccountResponse;
import com.example.bank.common.enums.AccountStatus;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountRequest request);

    AccountResponse getAccountById(Long id);

    List<AccountResponse> getAccountsByCustomerId(
            Long customerId
    );

    List<AccountResponse> getAllAccounts();

    AccountResponse blockAccount(Long id);

    AccountResponse activateAccount(Long id);

    AccountResponse closeAccount(Long id);

    AccountResponse deactivateAccount(Long id);
}