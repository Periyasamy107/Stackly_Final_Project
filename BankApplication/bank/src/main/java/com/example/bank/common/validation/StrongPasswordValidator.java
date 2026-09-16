package com.example.bank.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator
        implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;

    private static final int MAX_LENGTH = 72;

    private static final Pattern UPPERCASE =
            Pattern.compile(".*[A-Z].*");

    private static final Pattern LOWERCASE =
            Pattern.compile(".*[a-z].*");

    private static final Pattern DIGIT =
            Pattern.compile(".*\\d.*");

    private static final Pattern SPECIAL_CHARACTER =
            Pattern.compile(".*[^A-Za-z0-9].*");

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context) {

        /*
         * Null and blank validation are handled separately
         * by @NotBlank.
         */
        if (password == null) {
            return true;
        }

        if (password.length() < MIN_LENGTH
                || password.length() > MAX_LENGTH) {
            return false;
        }

        if (Character.isWhitespace(password.charAt(0))
                || Character.isWhitespace(
                password.charAt(password.length() - 1))) {
            return false;
        }

        return UPPERCASE.matcher(password).matches()
                && LOWERCASE.matcher(password).matches()
                && DIGIT.matcher(password).matches()
                && SPECIAL_CHARACTER.matcher(password).matches();
    }
}