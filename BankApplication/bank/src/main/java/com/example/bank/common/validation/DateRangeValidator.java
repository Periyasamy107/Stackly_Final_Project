package com.example.bank.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Objects;

public class DateRangeValidator
        implements ConstraintValidator<ValidDateRange, Object> {

    private String startField;

    private String endField;

    private boolean allowEqual;

    @Override
    public void initialize(
            ValidDateRange annotation) {

        this.startField =
                annotation.startField();

        this.endField =
                annotation.endField();

        this.allowEqual =
                annotation.allowEqual();
    }

    @Override
    public boolean isValid(
            Object value,
            ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        Object startValue =
                readField(value, startField);

        Object endValue =
                readField(value, endField);

        /*
         * Nullability is intentionally handled by @NotNull
         * on the individual fields.
         */
        if (startValue == null || endValue == null) {
            return true;
        }

        if (!(startValue instanceof Temporal)
                || !(endValue instanceof Temporal)) {

            return false;
        }

        if (!startValue.getClass()
                .equals(endValue.getClass())) {

            return false;
        }

        int comparison =
                compareTemporal(
                        (Temporal) startValue,
                        (Temporal) endValue
                );

        boolean valid =
                allowEqual
                        ? comparison <= 0
                        : comparison < 0;

        if (!valid) {
            addFieldError(
                    context,
                    endField
            );
        }

        return valid;
    }

    private Object readField(
            Object target,
            String fieldName) {

        Objects.requireNonNull(
                fieldName,
                "Field name must not be null"
        );

        Class<?> currentClass =
                target.getClass();

        while (currentClass != null) {

            try {
                Field field =
                        currentClass.getDeclaredField(
                                fieldName
                        );

                field.trySetAccessible();

                return field.get(target);

            } catch (NoSuchFieldException exception) {
                currentClass =
                        currentClass.getSuperclass();

            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(
                        "Unable to access validation field: "
                                + fieldName,
                        exception
                );
            }
        }

        throw new IllegalArgumentException(
                "Validation field does not exist: "
                        + fieldName
        );
    }

    private int compareTemporal(
            Temporal start,
            Temporal end) {

        if (start instanceof LocalDate startDate
                && end instanceof LocalDate endDate) {

            return startDate.compareTo(endDate);
        }

        if (start instanceof LocalDateTime startDateTime
                && end instanceof LocalDateTime endDateTime) {

            return startDateTime.compareTo(
                    endDateTime
            );
        }

        throw new IllegalArgumentException(
                "Unsupported temporal type: "
                        + start.getClass().getName()
        );
    }

    private void addFieldError(
            ConstraintValidatorContext context,
            String fieldName) {

        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(
                        context.getDefaultConstraintMessageTemplate()
                )
                .addPropertyNode(fieldName)
                .addConstraintViolation();
    }
}