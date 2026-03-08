package com.emiplanner.service.impl;

import com.emiplanner.dto.calendar.*;
import com.emiplanner.config.CacheNames;
import com.emiplanner.entity.Loan;
import com.emiplanner.repository.LoanRepository;
import com.emiplanner.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarServiceImpl implements CalendarService {

    private final LoanRepository loanRepository;

    @Override
    @Cacheable(cacheNames = CacheNames.CALENDAR_YEAR, key = "#userId.toString() + ':' + #year")
    public YearCalendarResponse getYearCalendar(UUID userId, int year) {
        log.info("Generate year calendar request received. userId={}, year={}", userId, year);

        List<Loan> loans = loanRepository.findByUserId(userId);
        List<MonthSummary> months = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            MonthCalculation monthCalculation = calculateMonth(loans, year, month);

            months.add(
                    new MonthSummary(
                            month,
                            year,
                            monthCalculation.totalEmi()
                    )
            );
        }

        log.info("Year calendar generated successfully. userId={}, year={}, monthsGenerated={}", userId, year, months.size());
        return new YearCalendarResponse(year, months);
    }

    @Override
    @Cacheable(cacheNames = CacheNames.CALENDAR_MONTH, key = "#userId.toString() + ':' + #year + ':' + #month")
    public MonthBreakdownResponse getMonthBreakdown(UUID userId, int year, int month) {
        log.info("Generate month breakdown request received. userId={}, year={}, month={}", userId, year, month);

        List<Loan> loans = loanRepository.findByUserId(userId);
        MonthCalculation monthCalculation = calculateMonth(loans, year, month);

        log.info(
                "Month breakdown generated successfully. userId={}, year={}, month={}, totalEmi={}, contributionCount={}",
                userId, year, month, monthCalculation.totalEmi(), monthCalculation.contributions().size()
        );
        return new MonthBreakdownResponse(
                month,
                year,
                monthCalculation.totalEmi(),
                monthCalculation.contributions()
        );
    }

    private MonthCalculation calculateMonth(List<Loan> loans, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        BigDecimal totalEmi = BigDecimal.ZERO;
        List<LoanContribution> contributions = new ArrayList<>();

        for (Loan loan : loans) {
            if (!isLoanActiveInRange(loan, monthStart, monthEnd)) {
                continue;
            }

            totalEmi = totalEmi.add(loan.getEmiAmount());
            contributions.add(new LoanContribution(loan.getId(), loan.getLoanName(), loan.getEmiAmount()));
        }

        return new MonthCalculation(totalEmi, contributions);
    }

    private boolean isLoanActiveInRange(Loan loan, LocalDate rangeStart, LocalDate rangeEnd) {
        LocalDate loanStart = loan.getStartDate();
        LocalDate loanEnd = loan.getClosedDate() != null ? loan.getClosedDate() : loan.getEndDate();

        return !loanStart.isAfter(rangeEnd) && !loanEnd.isBefore(rangeStart);
    }

    private record MonthCalculation(BigDecimal totalEmi, List<LoanContribution> contributions) {
    }
}
