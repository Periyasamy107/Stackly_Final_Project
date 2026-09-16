package com.example.bank.common.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static <T> Set<ConstraintViolation<T>> validate(
            Validator validator,
            T object) {

        if (validator == null) {
            throw new IllegalArgumentException(
                    "Validator must not be null"
            );
        }

        if (object == null) {
            throw new IllegalArgumentException(
                    "Object to validate must not be null"
            );
        }

        return validator.validate(object);
    }

    public static <T> boolean isValid(
            Validator validator,
            T object) {

        return validate(
                validator,
                object
        ).isEmpty();
    }

    public static <T> Map<String, String> getErrors(
            Validator validator,
            T object) {

        Set<ConstraintViolation<T>> violations =
                validate(
                        validator,
                        object
                );

        if (violations.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> errors =
                new LinkedHashMap<>();

        for (ConstraintViolation<T> violation :
                violations) {

            String propertyPath =
                    violation
                            .getPropertyPath()
                            .toString();

            String message =
                    violation.getMessage();

            errors.putIfAbsent(
                    propertyPath,
                    message
            );
        }

        return Collections.unmodifiableMap(
                errors
        );
    }

    public static <T> void requireValid(
            Validator validator,
            T object) {

        Map<String, String> errors =
                getErrors(
                        validator,
                        object
                );

        if (!errors.isEmpty()) {

            throw new IllegalArgumentException(
                    "Validation failed: " + errors
            );
        }
    }
}