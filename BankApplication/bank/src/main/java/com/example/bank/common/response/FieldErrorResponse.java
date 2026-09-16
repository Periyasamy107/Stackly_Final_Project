package com.example.bank.common.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldErrorResponse {

    private String field;

    private String rejectedValue;

    private String message;

    private String code;
}