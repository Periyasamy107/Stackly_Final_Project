package com.example.bank.reporting.dto.response;

import com.example.bank.common.enums.CustomerStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReportResponse {

    private Long customerId;

    private Long userId;

    private String username;

    private String name;

    private String email;

    private String phone;

    private String address;

    private CustomerStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer totalAccounts;

    private Integer activeAccounts;

    private Integer inactiveAccounts;

    private Integer blockedAccounts;

    private Integer closedAccounts;

    private BigDecimal totalBalance;

    private BigDecimal activeBalance;
}