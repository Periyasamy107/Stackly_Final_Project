package com.example.bank.account.service;

import com.example.bank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AccountNumberGeneratorImpl
        implements AccountNumberGenerator {

    private static final long MIN_ACCOUNT_NUMBER = 100_000_000_000L;
    private static final long MAX_ACCOUNT_NUMBER = 999_999_999_999L;

    private final AccountRepository accountRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {

        for (int attempt = 0; attempt < 10; attempt++) {

            long number = MIN_ACCOUNT_NUMBER
                    + secureRandom.nextLong(
                    MAX_ACCOUNT_NUMBER - MIN_ACCOUNT_NUMBER + 1L
            );

            String accountNumber = String.valueOf(number);

            if (!accountRepository.existsByAccountNumber(
                    accountNumber)) {

                return accountNumber;
            }
        }

        throw new IllegalStateException(
                "Unable to generate a unique account number"
        );
    }
}