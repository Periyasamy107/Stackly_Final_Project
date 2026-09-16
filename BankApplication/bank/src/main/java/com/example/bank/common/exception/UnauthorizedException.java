package com.example.bank.common.exception;

public class UnauthorizedException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "UNAUTHORIZED";

    public UnauthorizedException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }
}