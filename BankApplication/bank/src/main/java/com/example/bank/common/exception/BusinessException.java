package com.example.bank.common.exception;

public class BusinessException extends ApplicationException {

    public static final String ERROR_CODE =
            "BUSINESS_ERROR";

    public BusinessException(String message) {
        super(message, ERROR_CODE);
    }

    public BusinessException(
            String message,
            Throwable cause) {

        super(
                message,
                ERROR_CODE,
                cause
        );
    }
}