package com.example.bank.employee.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private Long id;
    private Long userId;
    private String employeeCode;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String designation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}