package com.example.bank.common.exception;

public class InvalidTransactionException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "INVALID_TRANSACTION";

    public InvalidTransactionException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }
}