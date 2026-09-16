package com.example.bank.investmentperformance.service;

import com.example.bank.common.exception.BusinessException;
import com.example.bank.common.util.DateTimeUtil;
import com.example.bank.investment.entity.Investment;
import com.example.bank.common.enums.InvestmentStatus;
import com.example.bank.investment.repository.InvestmentRepository;
import com.example.bank.investmentperformance.dto.request.InvestmentPerformanceRequest;
import com.example.bank.investmentperformance.dto.response.InvestmentPerformanceResponse;
import com.example.bank.investmentperformance.entity.InvestmentPerformance;
import com.example.bank.investmentperformance.mapper.InvestmentPerformanceMapper;
import com.example.bank.investmentperformance.repository.InvestmentPerformanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestmentPerformanceServiceImpl
        implements InvestmentPerformanceService {

    private static final int MONEY_SCALE = 4;
    private static final int RETURN_SCALE = 8;

    private final InvestmentPerformanceRepository performanceRepository;

    private final InvestmentRepository investmentRepository;

    private final InvestmentPerformanceMapper performanceMapper;

    @Override
    @Transactional
    public InvestmentPerformanceResponse recordPerformance(
            InvestmentPerformanceRequest request) {

        validatePerformanceRequest(request);

        Investment investment =
                investmentRepository
                        .findById(request.getInvestmentId())
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Investment not found: "
                                                + request.getInvestmentId()
                                ));

        validateInvestmentForPerformance(
                investment
        );

        LocalDate performanceDate =
                request.getPerformanceDate();

        if (performanceDate.isBefore(
                investment.getStartDate())) {

            throw new BusinessException(
                    "Performance date cannot be before investment start date"
            );
        }

        if (performanceRepository
                .existsByInvestmentIdAndPerformanceDate(
                        investment.getId(),
                        performanceDate
                )) {

            throw new BusinessException(
                    "Performance already exists for the investment on "
                            + performanceDate
            );
        }

        BigDecimal investedAmount =
                normalizeMoney(
                        investment.getAmount()
                );

        BigDecimal currentValue =
                normalizeMoney(
                        request.getCurrentValue()
                );

        BigDecimal profitLossAmount =
                currentValue
                        .subtract(investedAmount)
                        .setScale(
                                MONEY_SCALE,
                                RoundingMode.HALF_UP
                        );

        BigDecimal returnPercentage =
                calculateReturnPercentage(
                        profitLossAmount,
                        investedAmount
                );

        InvestmentPerformance performance =
                InvestmentPerformance.builder()
                        .investment(investment)
                        .performanceDate(performanceDate)
                        .investedAmount(investedAmount)
                        .currentValue(currentValue)
                        .profitLossAmount(profitLossAmount)
                        .returnPercentage(returnPercentage)
                        .notes(request.getNotes())
                        .build();

        InvestmentPerformance saved =
                performanceRepository.save(
                        performance
                );

        return performanceMapper.toResponse(
                saved
        );
    }

    @Override
    public InvestmentPerformanceResponse getPerformanceById(
            Long performanceId) {

        InvestmentPerformance performance =
                performanceRepository
                        .findById(performanceId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Investment performance not found: "
                                                + performanceId
                                ));

        return performanceMapper.toResponse(
                performance
        );
    }

    @Override
    public List<InvestmentPerformanceResponse>
    getInvestmentPerformance(
            Long investmentId) {

        ensureInvestmentExists(
                investmentId
        );

        return performanceRepository
                .findByInvestmentIdOrderByPerformanceDateDesc(
                        investmentId
                )
                .stream()
                .map(performanceMapper::toResponse)
                .toList();
    }

    @Override
    public InvestmentPerformanceResponse getLatestPerformance(
            Long investmentId) {

        ensureInvestmentExists(
                investmentId
        );

        InvestmentPerformance performance =
                performanceRepository
                        .findFirstByInvestmentIdOrderByPerformanceDateDesc(
                                investmentId
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        "No performance record exists "
                                                + "for investment: "
                                                + investmentId
                                ));

        return performanceMapper.toResponse(
                performance
        );
    }

    private void validatePerformanceRequest(
            InvestmentPerformanceRequest request) {

        if (request == null) {

            throw new BusinessException(
                    "Investment performance request is required"
            );
        }

        if (request.getInvestmentId() == null) {

            throw new BusinessException(
                    "Investment ID is required"
            );
        }

        if (request.getPerformanceDate() == null) {

            throw new BusinessException(
                    "Performance date is required"
            );
        }

        if (request.getCurrentValue() == null) {

            throw new BusinessException(
                    "Current value is required"
            );
        }

        if (request.getCurrentValue()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Current value must be greater than zero"
            );
        }

        if (request.getCurrentValue().scale()
                > MONEY_SCALE) {

            throw new BusinessException(
                    "Current value cannot have more than "
                            + MONEY_SCALE
                            + " decimal places"
            );
        }

        if (request.getPerformanceDate()
                .isAfter(DateTimeUtil.todayUtc())) {

            throw new BusinessException(
                    "Performance date cannot be in the future"
            );
        }
    }

    private void validateInvestmentForPerformance(
            Investment investment) {

        if (investment.getStatus()
                == InvestmentStatus.CLOSED) {

            throw new BusinessException(
                    "Cannot record performance for a CLOSED investment"
            );
        }

        if (investment.getStatus()
                == InvestmentStatus.CANCELLED) {

            throw new BusinessException(
                    "Cannot record performance for a CANCELLED investment"
            );
        }

        if (investment.getStartDate() == null) {

            throw new BusinessException(
                    "Investment start date is missing"
            );
        }
    }

    private void ensureInvestmentExists(
            Long investmentId) {

        if (investmentId == null) {

            throw new BusinessException(
                    "Investment ID is required"
            );
        }

        if (!investmentRepository.existsById(
                investmentId)) {

            throw new BusinessException(
                    "Investment not found: "
                            + investmentId
            );
        }
    }

    private BigDecimal calculateReturnPercentage(
            BigDecimal profitLossAmount,
            BigDecimal investedAmount) {

        if (investedAmount.compareTo(
                BigDecimal.ZERO) == 0) {

            throw new BusinessException(
                    "Invested amount cannot be zero"
            );
        }

        return profitLossAmount
                .divide(
                        investedAmount,
                        RETURN_SCALE,
                        RoundingMode.HALF_UP
                )
                .multiply(BigDecimal.valueOf(100))
                .setScale(
                        RETURN_SCALE,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal normalizeMoney(
            BigDecimal amount) {

        return amount.setScale(
                MONEY_SCALE,
                RoundingMode.HALF_UP
        );
    }
}