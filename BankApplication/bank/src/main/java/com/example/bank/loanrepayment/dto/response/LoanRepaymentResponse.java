package com.example.bank.loanrepayment.dto.response;

import com.example.bank.common.enums.LoanRepaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanRepaymentResponse {

    private Long id;

    private Long loanId;

    private Long accountId;

    private String accountNumber;

    private String transactionReference;

    private BigDecimal amount;

    private LocalDateTime repaymentDate;

    private LoanRepaymentStatus status;

    private String description;

    private LocalDateTime createdAt;

    private Long version;
}