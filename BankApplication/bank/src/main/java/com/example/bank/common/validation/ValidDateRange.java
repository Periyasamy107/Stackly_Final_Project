package com.example.bank.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({
        TYPE,
        ANNOTATION_TYPE
})
@Retention(RUNTIME)
public @interface ValidDateRange {

    String message() default
            "End date must be greater than or equal to start date";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String startField() default "startDate";

    String endField() default "endDate";

    boolean allowEqual() default true;
}