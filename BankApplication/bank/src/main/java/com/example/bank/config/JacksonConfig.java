package com.example.bank.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final String DATE_PATTERN =
            "yyyy-MM-dd";

    private static final String DATE_TIME_PATTERN =
            "yyyy-MM-dd'T'HH:mm:ss";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    @Bean
    public Module bankingJacksonModule() {

        SimpleModule module =
                new SimpleModule("BankingJacksonModule");

        module.addSerializer(
                BigDecimal.class,
                new BigDecimalSerializer()
        );

        return module;
    }

    @Bean
    public ObjectMapper objectMapper() {

        JavaTimeModule javaTimeModule =
                new JavaTimeModule();

        javaTimeModule.addSerializer(
                LocalDate.class,
                new LocalDateSerializer(
                        DATE_FORMATTER
                )
        );

        javaTimeModule.addDeserializer(
                LocalDate.class,
                new LocalDateDeserializer(
                        DATE_FORMATTER
                )
        );

        javaTimeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(
                        DATE_TIME_FORMATTER
                )
        );

        javaTimeModule.addDeserializer(
                LocalDateTime.class,
                new LocalDateTimeDeserializer(
                        DATE_TIME_FORMATTER
                )
        );

        ObjectMapper objectMapper =
                new ObjectMapper();

        objectMapper.registerModule(
                javaTimeModule
        );

        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        objectMapper.disable(
                SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
        );

        objectMapper.disable(
                DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS
        );

        objectMapper.enable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
        );

        objectMapper.enable(
                DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
        );

        objectMapper.enable(
                DeserializationFeature.USE_BIG_INTEGER_FOR_INTS
        );

        objectMapper.setSerializationInclusion(
                JsonInclude.Include.NON_NULL
        );

        return objectMapper;
    }

    private static final class LocalDateSerializer
            extends com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer {

        private LocalDateSerializer(
                DateTimeFormatter formatter) {

            super(formatter);
        }
    }

    private static final class LocalDateDeserializer
            extends com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer {

        private LocalDateDeserializer(
                DateTimeFormatter formatter) {

            super(formatter);
        }
    }

    private static final class LocalDateTimeSerializer
            extends com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer {

        private LocalDateTimeSerializer(
                DateTimeFormatter formatter) {

            super(formatter);
        }
    }

    private static final class LocalDateTimeDeserializer
            extends com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer {

        private LocalDateTimeDeserializer(
                DateTimeFormatter formatter) {

            super(formatter);
        }
    }
}