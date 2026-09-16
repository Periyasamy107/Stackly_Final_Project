package com.example.bank.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class MoneyUtil {

    public static final int MONEY_SCALE = 4;

    public static final RoundingMode MONEY_ROUNDING_MODE =
            RoundingMode.HALF_EVEN;

    private MoneyUtil() {
    }

    public static BigDecimal normalize(
            BigDecimal amount) {

        requireNonNull(amount);

        return amount.setScale(
                MONEY_SCALE,
                MONEY_ROUNDING_MODE
        );
    }

    public static BigDecimal zero() {

        return BigDecimal.ZERO.setScale(
                MONEY_SCALE,
                MONEY_ROUNDING_MODE
        );
    }

    public static boolean isZero(
            BigDecimal amount) {

        return normalize(amount)
                .compareTo(BigDecimal.ZERO) == 0;
    }

    public static boolean isPositive(
            BigDecimal amount) {

        return normalize(amount)
                .compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNegative(
            BigDecimal amount) {

        return normalize(amount)
                .compareTo(BigDecimal.ZERO) < 0;
    }

    public static boolean isNonNegative(
            BigDecimal amount) {

        return normalize(amount)
                .compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean isGreaterThan(
            BigDecimal first,
            BigDecimal second) {

        requireNonNull(first);
        requireNonNull(second);

        return first.compareTo(second) > 0;
    }

    public static boolean isGreaterThanOrEqual(
            BigDecimal first,
            BigDecimal second) {

        requireNonNull(first);
        requireNonNull(second);

        return first.compareTo(second) >= 0;
    }

    public static boolean isLessThan(
            BigDecimal first,
            BigDecimal second) {

        requireNonNull(first);
        requireNonNull(second);

        return first.compareTo(second) < 0;
    }

    public static boolean isLessThanOrEqual(
            BigDecimal first,
            BigDecimal second) {

        requireNonNull(first);
        requireNonNull(second);

        return first.compareTo(second) <= 0;
    }

    public static BigDecimal add(
            BigDecimal first,
            BigDecimal second) {

        requireNonNull(first);
        requireNonNull(second);

        return normalize(
                first.add(second)
        );
    }

    public static BigDecimal subtract(
            BigDecimal first,
            BigDecimal second) {

        requireNonNull(first);
        requireNonNull(second);

        return normalize(
                first.subtract(second)
        );
    }

    public static BigDecimal multiply(
            BigDecimal amount,
            BigDecimal multiplier) {

        requireNonNull(amount);
        requireNonNull(multiplier);

        return normalize(
                amount.multiply(multiplier)
        );
    }

    public static BigDecimal divide(
            BigDecimal amount,
            BigDecimal divisor) {

        requireNonNull(amount);
        requireNonNull(divisor);

        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException(
                    "Division by zero"
            );
        }

        return amount
                .divide(
                        divisor,
                        MONEY_SCALE,
                        MONEY_ROUNDING_MODE
                );
    }

    public static BigDecimal min(
            BigDecimal first,
            BigDecimal second) {

        requireNonNull(first);
        requireNonNull(second);

        return normalize(
                first.min(second)
        );
    }

    public static BigDecimal max(
            BigDecimal first,
            BigDecimal second) {

        requireNonNull(first);
        requireNonNull(second);

        return normalize(
                first.max(second)
        );
    }

    public static BigDecimal absolute(
            BigDecimal amount) {

        requireNonNull(amount);

        return normalize(
                amount.abs()
        );
    }

    public static BigDecimal negate(
            BigDecimal amount) {

        requireNonNull(amount);

        return normalize(
                amount.negate()
        );
    }

    public static void requireNonNull(
            BigDecimal amount) {

        Objects.requireNonNull(
                amount,
                "Money amount must not be null"
        );
    }

    public static void requirePositive(
            BigDecimal amount) {

        requireNonNull(amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Money amount must be positive"
            );
        }
    }

    public static void requireNonNegative(
            BigDecimal amount) {

        requireNonNull(amount);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Money amount must not be negative"
            );
        }
    }
}