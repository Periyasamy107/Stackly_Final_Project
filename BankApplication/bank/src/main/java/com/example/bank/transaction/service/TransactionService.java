package com.example.bank.transaction.service;

import com.example.bank.transaction.dto.request.TransactionRequest;
import com.example.bank.transaction.dto.request.TransferRequest;
import com.example.bank.transaction.dto.response.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    TransactionResponse deposit(
            TransactionRequest request
    );

    TransactionResponse withdraw(
            TransactionRequest request
    );

    List<TransactionResponse> transfer(
            TransferRequest request
    );

    TransactionResponse getTransactionById(
            Long id
    );

    List<TransactionResponse> getAccountTransactions(
            Long accountId
    );

    void creditLoanDisbursement(
            Long accountId,
            BigDecimal amount,
            String transactionReference,
            String description
    );

    TransactionResponse creditInvestmentSettlement(
            Long accountId,
            BigDecimal amount,
            String investmentReference
    );

    TransactionResponse reverseTransaction(
            Long transactionId
    );
}