package com.emiplanner.service;

import com.emiplanner.dto.loan.LoanCloseRequest;
import com.emiplanner.dto.loan.LoanCreateRequest;
import com.emiplanner.dto.loan.LoanResponse;
import com.emiplanner.dto.loan.LoanUpdateRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanService {

    LoanResponse createLoan(UUID userId, LoanCreateRequest request);

    LoanResponse updateLoan(UUID loanId, UUID userId, LoanUpdateRequest request);

    void deleteLoan(UUID loanId, UUID userId);

    LoanResponse closeLoan(UUID loanId, UUID userId, LoanCloseRequest request);

    LoanResponse getLoanById(UUID loanId, UUID userId);

    List<LoanResponse> getUserLoans(UUID userId);

}
