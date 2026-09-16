package com.example.bank.common.exception;

public class ForbiddenException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "FORBIDDEN";

    public ForbiddenException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }
}