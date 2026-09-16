package com.example.bank.common.exception;

public class ValidationException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "VALIDATION_ERROR";

    public ValidationException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }
}