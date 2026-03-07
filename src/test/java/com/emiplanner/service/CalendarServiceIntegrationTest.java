package com.emiplanner.service;

import com.emiplanner.dto.auth.RegisterRequest;
import com.emiplanner.dto.calendar.MonthBreakdownResponse;
import com.emiplanner.dto.calendar.MonthSummary;
import com.emiplanner.dto.calendar.YearCalendarResponse;
import com.emiplanner.dto.loan.LoanCloseRequest;
import com.emiplanner.dto.loan.LoanCreateRequest;
import com.emiplanner.dto.loan.LoanResponse;
import com.emiplanner.dto.user.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CalendarServiceIntegrationTest {

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private AuthService authService;

    @Test
    void getYearCalendar_shouldComputeTotalsForAllMonths() {
        UUID userId = registerUser("Alice").getId();

        LoanResponse homeLoan = loanService.createLoan(userId, LoanCreateRequest.builder()
                .loanName("Home Loan")
                .providerName("Bank A")
                .emiAmount(new BigDecimal("10000"))
                .startDate(LocalDate.of(2025, 1, 1))
                .tenureMonths(24)
                .build());

        loanService.createLoan(userId, LoanCreateRequest.builder()
                .loanName("Car Loan")
                .providerName("Bank B")
                .emiAmount(new BigDecimal("5000"))
                .startDate(LocalDate.of(2025, 3, 1))
                .tenureMonths(10)
                .build());

        loanService.closeLoan(homeLoan.getId(), userId, LoanCloseRequest.builder()
                .closedDate(LocalDate.of(2025, 6, 15))
                .build());

        YearCalendarResponse response = calendarService.getYearCalendar(userId, 2025);

        assertThat(response.getYear()).isEqualTo(2025);
        assertThat(response.getMonths()).hasSize(12);

        Map<Integer, BigDecimal> totals = response.getMonths().stream()
                .sorted(Comparator.comparing(MonthSummary::getMonth))
                .collect(Collectors.toMap(MonthSummary::getMonth, MonthSummary::getTotalEmiAmount));

        assertThat(totals.get(1)).isEqualByComparingTo("10000");
        assertThat(totals.get(2)).isEqualByComparingTo("10000");
        assertThat(totals.get(3)).isEqualByComparingTo("15000");
        assertThat(totals.get(6)).isEqualByComparingTo("15000");
        assertThat(totals.get(7)).isEqualByComparingTo("5000");
        assertThat(totals.get(12)).isEqualByComparingTo("5000");
    }

    @Test
    void getMonthBreakdown_shouldReturnContributionsForGivenMonth() {
        UUID userId = registerUser("Alice").getId();

        loanService.createLoan(userId, LoanCreateRequest.builder()
                .loanName("Home Loan")
                .providerName("Bank A")
                .emiAmount(new BigDecimal("10000"))
                .startDate(LocalDate.of(2025, 1, 1))
                .tenureMonths(24)
                .build());

        loanService.createLoan(userId, LoanCreateRequest.builder()
                .loanName("Car Loan")
                .providerName("Bank B")
                .emiAmount(new BigDecimal("5000"))
                .startDate(LocalDate.of(2025, 3, 1))
                .tenureMonths(10)
                .build());

        MonthBreakdownResponse march = calendarService.getMonthBreakdown(userId, 2025, 3);
        MonthBreakdownResponse december = calendarService.getMonthBreakdown(userId, 2025, 12);

        assertThat(march.getMonth()).isEqualTo(3);
        assertThat(march.getLoans()).hasSize(2);
        assertThat(march.getTotalEmiAmount()).isEqualByComparingTo("15000");
        assertThat(march.getLoans().stream().map(l -> l.getLoanName()))
                .containsExactlyInAnyOrder("Home Loan", "Car Loan");

        assertThat(december.getLoans()).hasSize(2);
        assertThat(december.getTotalEmiAmount()).isEqualByComparingTo("15000");
    }

    private UserResponse registerUser(String name) {
        return authService.register(RegisterRequest.builder()
                .name(name)
                .phoneNumber(uniquePhone())
                .password("password123")
                .build());
    }

    private String uniquePhone() {
        long value = Math.abs(System.nanoTime() % 1_000_000_0000L);
        return String.format("%010d", value);
    }
}
