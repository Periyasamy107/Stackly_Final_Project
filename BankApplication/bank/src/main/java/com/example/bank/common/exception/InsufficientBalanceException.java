package com.example.bank.common.exception;

public class InsufficientBalanceException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "INSUFFICIENT_BALANCE";

    public InsufficientBalanceException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }
}