package com.example.bank.scheduler;

import com.example.bank.common.enums.InvestmentStatus;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.investment.entity.Investment;
import com.example.bank.investment.repository.InvestmentRepository;
import com.example.bank.investment.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvestmentScheduler {

    private final InvestmentRepository investmentRepository;

    private final InvestmentService investmentService;

    /**
     * Finds active investments whose maturity date has arrived
     * and moves them to MATURED through the normal business service.
     *
     * Fixed-delay execution prevents overlapping executions
     * within the same application instance.
     */
    @Scheduled(
            fixedDelayString = "${bank.scheduler.investment-maturity-delay-ms:3600000}"
    )
    public void matureDueInvestments() {

        LocalDate today = DateTimeUtil.todayUtc();

        List<Investment> dueInvestments =
                investmentRepository
                        .findByStatusAndMaturityDateLessThanEqual(
                                InvestmentStatus.ACTIVE,
                                today
                        );

        if (dueInvestments.isEmpty()) {
            log.debug(
                    "Investment maturity scheduler found no due investments"
            );
            return;
        }

        log.info(
                "Investment maturity scheduler found {} due investments",
                dueInvestments.size()
        );

        for (Investment investment : dueInvestments) {

            try {

                investmentService.matureInvestment(
                        investment.getId()
                );

                log.info(
                        "Investment {} automatically matured",
                        investment.getId()
                );

            } catch (Exception exception) {

                log.error(
                        "Failed to mature investment {}",
                        investment.getId(),
                        exception
                );
            }
        }
    }
}