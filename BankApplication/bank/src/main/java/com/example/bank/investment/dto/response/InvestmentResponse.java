package com.example.bank.investment.dto.response;

import com.example.bank.common.enums.InvestmentStatus;
import com.example.bank.common.enums.InvestmentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentResponse {

    private Long id;

    private Long customerId;

    private Long accountId;

    private String accountNumber;

    private InvestmentType investmentType;

    private BigDecimal amount;

    private LocalDate startDate;

    private LocalDate maturityDate;

    private InvestmentStatus status;

    private String description;

    private String settlementReference;

    private LocalDateTime settledAt;

    private Long settlementAccountId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long version;
}