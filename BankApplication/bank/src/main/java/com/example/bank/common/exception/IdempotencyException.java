package com.example.bank.common.exception;

public class IdempotencyException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "IDEMPOTENCY_ERROR";

    public IdempotencyException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }
}