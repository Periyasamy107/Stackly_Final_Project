package com.example.bank.scheduler;

import com.example.bank.common.enums.LoanStatus;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.loan.entity.Loan;
import com.example.bank.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanScheduler {

    private final LoanRepository loanRepository;

    /**
     * Monitors active loans whose contractual end date has passed.
     *
     * This scheduler deliberately does not change the loan status.
     * Automatic DEFAULTED/COMPLETED transitions require a defined
     * repayment and delinquency policy.
     */
    @Scheduled(
            fixedDelayString = "${bank.scheduler.loan-monitor-delay-ms:3600000}"
    )
    public void monitorOverdueLoans() {

        LocalDate today = DateTimeUtil.todayUtc();

        List<Loan> overdueLoans =
                loanRepository.findByStatus(
                                LoanStatus.ACTIVE
                        )
                        .stream()
                        .filter(loan ->
                                loan.getEndDate() != null
                                        && loan.getEndDate().isBefore(today)
                        )
                        .toList();

        if (overdueLoans.isEmpty()) {
            log.debug(
                    "Loan scheduler found no overdue active loans"
            );
            return;
        }

        log.warn(
                "Loan scheduler detected {} overdue active loans",
                overdueLoans.size()
        );

        overdueLoans.forEach(
                loan -> log.warn(
                        "Loan {} for customer {} is past its end date",
                        loan.getId(),
                        loan.getCustomer().getId()
                )
        );
    }
}