package com.example.bank.common.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String code;

    private String message;

    private String path;

    private String traceId;

    private List<FieldErrorResponse> fieldErrors;
}