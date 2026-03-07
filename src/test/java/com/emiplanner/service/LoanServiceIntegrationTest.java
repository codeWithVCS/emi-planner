package com.emiplanner.service;

import com.emiplanner.dto.auth.RegisterRequest;
import com.emiplanner.dto.loan.LoanCloseRequest;
import com.emiplanner.dto.loan.LoanCreateRequest;
import com.emiplanner.dto.loan.LoanResponse;
import com.emiplanner.dto.loan.LoanUpdateRequest;
import com.emiplanner.dto.user.UserResponse;
import com.emiplanner.entity.LoanStatus;
import com.emiplanner.exception.AuthorizationException;
import com.emiplanner.exception.BusinessRuleException;
import com.emiplanner.exception.DuplicateResourceException;
import com.emiplanner.exception.ResourceNotFoundException;
import com.emiplanner.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanServiceIntegrationTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    void createLoan_shouldPersistLoan() {
        UUID userId = registerUser("Alice").getId();

        LoanResponse response = loanService.createLoan(userId, createRequest("Home Loan", LocalDate.of(2025, 1, 1), 12, "8000"));

        assertThat(response.getId()).isNotNull();
        assertThat(response.getLoanName()).isEqualTo("Home Loan");
        assertThat(response.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loanRepository.findById(response.getId())).isPresent();
    }

    @Test
    void createLoan_shouldThrowForDuplicateLoan() {
        UUID userId = registerUser("Alice").getId();
        LoanCreateRequest request = createRequest("Home Loan", LocalDate.of(2025, 1, 1), 12, "8000");

        loanService.createLoan(userId, request);

        assertThrows(DuplicateResourceException.class, () -> loanService.createLoan(userId, request));
    }

    @Test
    void updateLoan_shouldUpdateFields() {
        UUID userId = registerUser("Alice").getId();
        LoanResponse created = loanService.createLoan(userId, createRequest("Home Loan", LocalDate.of(2025, 1, 1), 12, "8000"));

        LoanUpdateRequest update = LoanUpdateRequest.builder()
                .loanName("Home Loan Updated")
                .providerName("Bank B")
                .emiAmount(new BigDecimal("9000"))
                .startDate(LocalDate.of(2025, 2, 1))
                .tenureMonths(24)
                .build();

        LoanResponse updated = loanService.updateLoan(created.getId(), userId, update);

        assertThat(updated.getLoanName()).isEqualTo("Home Loan Updated");
        assertThat(updated.getProviderName()).isEqualTo("Bank B");
        assertThat(updated.getEndDate()).isEqualTo(LocalDate.of(2027, 2, 1));
    }

    @Test
    void updateLoan_shouldThrowForUnauthorizedUser() {
        UUID ownerId = registerUser("Owner").getId();
        UUID anotherUserId = registerUser("Another").getId();
        LoanResponse created = loanService.createLoan(ownerId, createRequest("Home Loan", LocalDate.of(2025, 1, 1), 12, "8000"));

        LoanUpdateRequest update = LoanUpdateRequest.builder()
                .loanName("Changed")
                .providerName("Bank")
                .emiAmount(new BigDecimal("9000"))
                .startDate(LocalDate.of(2025, 1, 1))
                .tenureMonths(12)
                .build();

        assertThrows(AuthorizationException.class, () -> loanService.updateLoan(created.getId(), anotherUserId, update));
    }

    @Test
    void deleteLoan_shouldRemoveLoan() {
        UUID userId = registerUser("Alice").getId();
        LoanResponse created = loanService.createLoan(userId, createRequest("Car Loan", LocalDate.of(2025, 1, 1), 24, "6000"));

        loanService.deleteLoan(created.getId(), userId);

        assertThat(loanRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void deleteLoan_shouldThrowForUnauthorizedUser() {
        UUID ownerId = registerUser("Owner").getId();
        UUID anotherUserId = registerUser("Another").getId();
        LoanResponse created = loanService.createLoan(ownerId, createRequest("Car Loan", LocalDate.of(2025, 1, 1), 24, "6000"));

        assertThrows(AuthorizationException.class, () -> loanService.deleteLoan(created.getId(), anotherUserId));
    }

    @Test
    void closeLoan_shouldMarkClosed() {
        UUID userId = registerUser("Alice").getId();
        LoanResponse created = loanService.createLoan(userId, createRequest("Bike Loan", LocalDate.of(2025, 1, 1), 12, "3000"));

        LoanResponse closed = loanService.closeLoan(created.getId(), userId, LoanCloseRequest.builder()
                .closedDate(LocalDate.of(2025, 6, 1))
                .build());

        assertThat(closed.getStatus()).isEqualTo(LoanStatus.CLOSED);
        assertThat(closed.getClosedDate()).isEqualTo(LocalDate.of(2025, 6, 1));
    }

    @Test
    void closeLoan_shouldThrowForInvalidClosedDate() {
        UUID userId = registerUser("Alice").getId();
        LoanResponse created = loanService.createLoan(userId, createRequest("Bike Loan", LocalDate.of(2025, 1, 1), 12, "3000"));

        assertThrows(BusinessRuleException.class, () -> loanService.closeLoan(created.getId(), userId, LoanCloseRequest.builder()
                .closedDate(LocalDate.of(2028, 1, 1))
                .build()));
    }

    @Test
    void getLoanById_shouldReturnLoanForOwner() {
        UUID userId = registerUser("Alice").getId();
        LoanResponse created = loanService.createLoan(userId, createRequest("Personal Loan", LocalDate.of(2025, 3, 1), 10, "4500"));

        LoanResponse found = loanService.getLoanById(created.getId(), userId);

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getLoanName()).isEqualTo("Personal Loan");
    }

    @Test
    void getLoanById_shouldThrowForUnauthorizedUser() {
        UUID ownerId = registerUser("Owner").getId();
        UUID anotherUserId = registerUser("Another").getId();
        LoanResponse created = loanService.createLoan(ownerId, createRequest("Personal Loan", LocalDate.of(2025, 3, 1), 10, "4500"));

        assertThrows(AuthorizationException.class, () -> loanService.getLoanById(created.getId(), anotherUserId));
    }

    @Test
    void getLoanById_shouldThrowWhenMissing() {
        UUID userId = registerUser("Alice").getId();
        assertThrows(ResourceNotFoundException.class, () -> loanService.getLoanById(UUID.randomUUID(), userId));
    }

    @Test
    void getUserLoans_shouldReturnPagedLoans() {
        UUID userId = registerUser("Alice").getId();
        loanService.createLoan(userId, createRequest("Loan A", LocalDate.of(2025, 1, 1), 12, "5000"));
        loanService.createLoan(userId, createRequest("Loan B", LocalDate.of(2025, 2, 1), 12, "6000"));
        loanService.createLoan(userId, createRequest("Loan C", LocalDate.of(2025, 3, 1), 12, "7000"));

        Page<LoanResponse> firstPage = loanService.getUserLoans(userId, 0, 2);
        Page<LoanResponse> secondPage = loanService.getUserLoans(userId, 1, 2);

        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(1);
    }

    private LoanCreateRequest createRequest(String loanName, LocalDate startDate, int tenureMonths, String emi) {
        return LoanCreateRequest.builder()
                .loanName(loanName)
                .providerName("Bank A")
                .emiAmount(new BigDecimal(emi))
                .startDate(startDate)
                .tenureMonths(tenureMonths)
                .build();
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
