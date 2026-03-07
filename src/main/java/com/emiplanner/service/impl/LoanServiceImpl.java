package com.emiplanner.service.impl;

import com.emiplanner.dto.loan.LoanCloseRequest;
import com.emiplanner.dto.loan.LoanCreateRequest;
import com.emiplanner.dto.loan.LoanResponse;
import com.emiplanner.dto.loan.LoanUpdateRequest;
import com.emiplanner.entity.Loan;
import com.emiplanner.entity.LoanStatus;
import com.emiplanner.entity.User;
import com.emiplanner.exception.AuthorizationException;
import com.emiplanner.exception.BusinessRuleException;
import com.emiplanner.exception.DuplicateResourceException;
import com.emiplanner.exception.ResourceNotFoundException;
import com.emiplanner.repository.LoanRepository;
import com.emiplanner.repository.UserRepository;
import com.emiplanner.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    @Override
    public LoanResponse createLoan(UUID userId, LoanCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(loanRepository.existsByUserIdAndLoanNameAndProviderNameAndStartDate(
                userId,
                request.getLoanName(),
                request.getProviderName(),
                request.getStartDate()
        )){
            throw new DuplicateResourceException("Loan with the provided details already exists");
        }

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setLoanName(request.getLoanName());
        loan.setProviderName(request.getProviderName());
        loan.setEmiAmount(request.getEmiAmount());
        loan.setStartDate(request.getStartDate());
        loan.setTenureMonths(request.getTenureMonths());

        Loan savedLoan = loanRepository.save(loan);

        return mapToLoanResponse(savedLoan);
    }

    @Override
    public LoanResponse updateLoan(UUID loanId, UUID userId, LoanUpdateRequest request) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Access Denied");
        }

        loan.setLoanName(request.getLoanName());
        loan.setProviderName(request.getProviderName());
        loan.setEmiAmount(request.getEmiAmount());
        loan.setStartDate(request.getStartDate());
        loan.setTenureMonths(request.getTenureMonths());

        LocalDate endDate = request.getStartDate().plusMonths(request.getTenureMonths());

        loan.setEndDate(endDate);

        Loan savedLoan = loanRepository.save(loan);

        return mapToLoanResponse(savedLoan);
    }

    @Override
    public void deleteLoan(UUID loanId, UUID userId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Access Denied");
        }

        loanRepository.delete(loan);
    }

    @Override
    public LoanResponse closeLoan(UUID loanId, UUID userId, LoanCloseRequest request) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Access Denied");
        }

        LocalDate startDate = loan.getStartDate();
        LocalDate endDate = loan.getEndDate();
        LocalDate closedDate = request.getClosedDate();

        if(closedDate.isBefore(startDate) || closedDate.isAfter(endDate)){
            throw new BusinessRuleException("Closed date must be within the loan period");
        }

        loan.setStatus(LoanStatus.CLOSED);
        loan.setClosedDate(request.getClosedDate());

        Loan savedLoan = loanRepository.save(loan);

        return mapToLoanResponse(savedLoan);
    }

    @Override
    public LoanResponse getLoanById(UUID loanId, UUID userId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new AuthorizationException("Access Denied");
        }

        return mapToLoanResponse(loan);
    }

    @Override
    public Page<LoanResponse> getUserLoans(UUID userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return loanRepository.findByUserId(userId, pageable)
                .map(this::mapToLoanResponse);
    }

    private LoanResponse mapToLoanResponse(Loan loan){
        LoanResponse response = new LoanResponse();

        response.setId(loan.getId());
        response.setLoanName(loan.getLoanName());
        response.setProviderName(loan.getProviderName());
        response.setEmiAmount(loan.getEmiAmount());
        response.setStartDate(loan.getStartDate());
        response.setTenureMonths(loan.getTenureMonths());
        response.setEndDate(loan.getEndDate());
        response.setClosedDate(loan.getClosedDate());
        response.setStatus(loan.getStatus());
        response.setCreatedAt(loan.getCreatedAt());
        response.setUpdatedAt(loan.getUpdatedAt());

        return response;
    }

}
