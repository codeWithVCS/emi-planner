package com.emiplanner.service;

import com.emiplanner.dto.loan.LoanCloseRequest;
import com.emiplanner.dto.loan.LoanCreateRequest;
import com.emiplanner.dto.loan.LoanResponse;
import com.emiplanner.dto.loan.LoanUpdateRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LoanService {

    LoanResponse createLoan(UUID userId, LoanCreateRequest request);

    LoanResponse updateLoan(UUID loanId, UUID userId, LoanUpdateRequest request);

    void deleteLoan(UUID loanId, UUID userId);

    LoanResponse closeLoan(UUID loanId, UUID userId, LoanCloseRequest request);

    LoanResponse getLoanById(UUID loanId, UUID userId);

    Page<LoanResponse> getUserLoans(UUID userId, int page, int size);

}
