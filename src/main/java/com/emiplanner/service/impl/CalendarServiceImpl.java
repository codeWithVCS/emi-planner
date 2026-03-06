package com.emiplanner.service.impl;

import com.emiplanner.dto.calendar.*;
import com.emiplanner.entity.Loan;
import com.emiplanner.repository.LoanRepository;
import com.emiplanner.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final LoanRepository loanRepository;

    @Override
    public YearCalendarResponse getYearCalendar(UUID userId, int year) {

        List<Loan> loans = loanRepository.findByUserId(userId);

        List<MonthSummary> months = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {

            YearMonth ym = YearMonth.of(year, month);

            LocalDate monthStart = ym.atDay(1);
            LocalDate monthEnd = ym.atEndOfMonth();

            BigDecimal totalEmi = BigDecimal.ZERO;

            for (Loan loan : loans) {

                LocalDate loanStart = loan.getStartDate();
                LocalDate loanEnd = loan.getClosedDate() != null
                        ? loan.getClosedDate()
                        : loan.getEndDate();

                boolean contributes =
                        !loanStart.isAfter(monthEnd) &&
                                !loanEnd.isBefore(monthStart);

                if (contributes) {
                    totalEmi = totalEmi.add(loan.getEmiAmount());
                }
            }

            months.add(
                    new MonthSummary(
                            month,
                            year,
                            totalEmi
                    )
            );
        }

        return new YearCalendarResponse(year, months);
    }

    @Override
    public MonthBreakdownResponse getMonthBreakdown(UUID userId, int year, int month) {

        List<Loan> loans = loanRepository.findByUserId(userId);

        YearMonth ym = YearMonth.of(year, month);

        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        BigDecimal totalEmi = BigDecimal.ZERO;
        List<LoanContribution> contributions = new ArrayList<>();

        for (Loan loan : loans) {

            LocalDate loanStart = loan.getStartDate();
            LocalDate loanEnd = loan.getClosedDate() != null
                    ? loan.getClosedDate()
                    : loan.getEndDate();

            boolean contributes =
                    !loanStart.isAfter(monthEnd) &&
                            !loanEnd.isBefore(monthStart);

            if (contributes) {

                totalEmi = totalEmi.add(loan.getEmiAmount());

                contributions.add(
                        new LoanContribution(
                                loan.getId(),
                                loan.getLoanName(),
                                loan.getEmiAmount()
                        )
                );
            }
        }

        return new MonthBreakdownResponse(
                month,
                year,
                totalEmi,
                contributions
        );
    }
}