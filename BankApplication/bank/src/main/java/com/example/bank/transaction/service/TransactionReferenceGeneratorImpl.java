package com.example.bank.transaction.service;

import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionReferenceGeneratorImpl
        implements TransactionReferenceGenerator {

    private final TransactionRepository transactionRepository;

    @Override
    public String generate() {

        for (int attempt = 0; attempt < 10; attempt++) {

            String reference =
                    "TXN-"
                            + DateTimeUtil.todayUtc()
                            + "-"
                            + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 16)
                            .toUpperCase();

            if (!transactionRepository
                    .existsByTransactionReference(reference)) {

                return reference;
            }
        }

        throw new IllegalStateException(
                "Unable to generate transaction reference"
        );
    }
}