package com.example.bank.common.exception;

public class LoanEligibilityException
        extends ApplicationException {

    public static final String ERROR_CODE =
            "LOAN_ELIGIBILITY_ERROR";

    public LoanEligibilityException(
            String message) {

        super(
                message,
                ERROR_CODE
        );
    }
}