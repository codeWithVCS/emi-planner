package com.emiplanner.repository;

import com.emiplanner.entity.Loan;
import com.emiplanner.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByUserId(UUID userId);

    List<Loan> findByUserIdAndStatus(UUID userId, LoanStatus status);

    boolean existsByUserIdAndLoanNameAndProviderNameAndStartDate(
            UUID userId,
            String loanName,
            String providerName,
            LocalDate startDate
    );

}
