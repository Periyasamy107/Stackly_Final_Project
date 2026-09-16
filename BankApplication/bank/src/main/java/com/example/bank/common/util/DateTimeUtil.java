package com.example.bank.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public final class DateTimeUtil {

    public static final ZoneOffset UTC =
            ZoneOffset.UTC;

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE;

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateTimeUtil() {
    }


    public static LocalDateTime nowUtc() {

        return LocalDateTime.now(UTC);
    }


    public static LocalDate todayUtc() {

        return LocalDate.now(UTC);
    }


    public static Instant instantNow() {

        return Instant.now();
    }


    public static LocalDateTime toUtcDateTime(
            Instant instant) {

        Objects.requireNonNull(
                instant,
                "Instant must not be null"
        );

        return LocalDateTime.ofInstant(
                instant,
                UTC
        );
    }


    public static Instant toInstant(
            LocalDateTime dateTime) {

        Objects.requireNonNull(
                dateTime,
                "DateTime must not be null"
        );

        return dateTime.toInstant(UTC);
    }


    public static LocalDateTime startOfDay(
            LocalDate date) {

        Objects.requireNonNull(
                date,
                "Date must not be null"
        );

        return date.atStartOfDay();
    }


    public static LocalDateTime endOfDay(
            LocalDate date) {

        Objects.requireNonNull(
                date,
                "Date must not be null"
        );

        return date.atTime(
                LocalTime.MAX
        );
    }


    public static LocalDate toDate(
            LocalDateTime dateTime) {

        Objects.requireNonNull(
                dateTime,
                "DateTime must not be null"
        );

        return dateTime.toLocalDate();
    }


    public static LocalDateTime atStartOfDay(
            LocalDate date) {

        return startOfDay(date);
    }


    public static LocalDateTime truncateToSeconds(
            LocalDateTime dateTime) {

        Objects.requireNonNull(
                dateTime,
                "DateTime must not be null"
        );

        return dateTime.withNano(0);
    }


    public static boolean isBefore(
            LocalDateTime first,
            LocalDateTime second) {

        Objects.requireNonNull(
                first,
                "First date/time must not be null"
        );

        Objects.requireNonNull(
                second,
                "Second date/time must not be null"
        );

        return first.isBefore(second);
    }


    public static boolean isAfter(
            LocalDateTime first,
            LocalDateTime second) {

        Objects.requireNonNull(
                first,
                "First date/time must not be null"
        );

        Objects.requireNonNull(
                second,
                "Second date/time must not be null"
        );

        return first.isAfter(second);
    }


    public static boolean isEqual(
            LocalDateTime first,
            LocalDateTime second) {

        Objects.requireNonNull(
                first,
                "First date/time must not be null"
        );

        Objects.requireNonNull(
                second,
                "Second date/time must not be null"
        );

        return first.isEqual(second);
    }


    public static boolean isDateBefore(
            LocalDate first,
            LocalDate second) {

        Objects.requireNonNull(
                first,
                "First date must not be null"
        );

        Objects.requireNonNull(
                second,
                "Second date must not be null"
        );

        return first.isBefore(second);
    }


    public static boolean isDateAfter(
            LocalDate first,
            LocalDate second) {

        Objects.requireNonNull(
                first,
                "First date must not be null"
        );

        Objects.requireNonNull(
                second,
                "Second date must not be null"
        );

        return first.isAfter(second);
    }


    public static boolean isTodayUtc(
            LocalDate date) {

        Objects.requireNonNull(
                date,
                "Date must not be null"
        );

        return date.equals(todayUtc());
    }


    public static String formatDate(
            LocalDate date) {

        Objects.requireNonNull(
                date,
                "Date must not be null"
        );

        return DATE_FORMATTER.format(date);
    }


    public static String formatDateTime(
            LocalDateTime dateTime) {

        Objects.requireNonNull(
                dateTime,
                "DateTime must not be null"
        );

        return DATE_TIME_FORMATTER.format(dateTime);
    }


    public static LocalDate parseDate(
            String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Date value must not be null or blank"
            );
        }

        try {
            return LocalDate.parse(
                    value.trim(),
                    DATE_FORMATTER
            );
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid date value: " + value,
                    exception
            );
        }
    }


    public static LocalDateTime parseDateTime(
            String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Date/time value must not be null or blank"
            );
        }

        try {
            return LocalDateTime.parse(
                    value.trim(),
                    DATE_TIME_FORMATTER
            );
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "Invalid date/time value: " + value,
                    exception
            );
        }
    }
}