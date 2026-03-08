package com.emiplanner.service.impl;

import com.emiplanner.dto.loan.LoanCloseRequest;
import com.emiplanner.dto.loan.LoanCreateRequest;
import com.emiplanner.dto.loan.LoanResponse;
import com.emiplanner.dto.loan.LoanUpdateRequest;
import com.emiplanner.config.CacheNames;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.CALENDAR_YEAR, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.CALENDAR_MONTH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.USER_LOANS, allEntries = true)
    })
    public LoanResponse createLoan(UUID userId, LoanCreateRequest request) {
        log.info(
                "Create loan request received. userId={}, loanName={}, providerName={}, startDate={}, tenureMonths={}, emiAmount={}",
                userId, request.getLoanName(), request.getProviderName(), request.getStartDate(), request.getTenureMonths(), request.getEmiAmount()
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Create loan failed: user not found. userId={}", userId);
                    return new ResourceNotFoundException("User not found");
                });

        if(loanRepository.existsByUserIdAndLoanNameAndProviderNameAndStartDate(
                userId,
                request.getLoanName(),
                request.getProviderName(),
                request.getStartDate()
        )){
            log.warn(
                    "Create loan failed: duplicate loan. userId={}, loanName={}, providerName={}, startDate={}",
                    userId, request.getLoanName(), request.getProviderName(), request.getStartDate()
            );
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
        log.info("Loan created successfully. loanId={}, userId={}", savedLoan.getId(), userId);

        return mapToLoanResponse(savedLoan);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.CALENDAR_YEAR, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.CALENDAR_MONTH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.USER_LOANS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOAN_BY_ID, key = "#userId.toString() + ':' + #loanId.toString()")
    })
    public LoanResponse updateLoan(UUID loanId, UUID userId, LoanUpdateRequest request) {
        log.info("Update loan request received. loanId={}, userId={}", loanId, userId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.warn("Update loan failed: loan not found. loanId={}", loanId);
                    return new ResourceNotFoundException("Loan not found");
                });

        if (!loan.getUser().getId().equals(userId)) {
            log.warn("Update loan failed: access denied. loanId={}, requesterUserId={}, ownerUserId={}", loanId, userId, loan.getUser().getId());
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
        log.info("Loan updated successfully. loanId={}, userId={}, endDate={}", loanId, userId, savedLoan.getEndDate());

        return mapToLoanResponse(savedLoan);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.CALENDAR_YEAR, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.CALENDAR_MONTH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.USER_LOANS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOAN_BY_ID, key = "#userId.toString() + ':' + #loanId.toString()")
    })
    public void deleteLoan(UUID loanId, UUID userId) {
        log.info("Delete loan request received. loanId={}, userId={}", loanId, userId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.warn("Delete loan failed: loan not found. loanId={}", loanId);
                    return new ResourceNotFoundException("Loan not found");
                });

        if (!loan.getUser().getId().equals(userId)) {
            log.warn("Delete loan failed: access denied. loanId={}, requesterUserId={}, ownerUserId={}", loanId, userId, loan.getUser().getId());
            throw new AuthorizationException("Access Denied");
        }

        loanRepository.delete(loan);
        log.info("Loan deleted successfully. loanId={}, userId={}", loanId, userId);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.CALENDAR_YEAR, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.CALENDAR_MONTH, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.USER_LOANS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.LOAN_BY_ID, key = "#userId.toString() + ':' + #loanId.toString()")
    })
    public LoanResponse closeLoan(UUID loanId, UUID userId, LoanCloseRequest request) {
        log.info("Close loan request received. loanId={}, userId={}, closedDate={}", loanId, userId, request.getClosedDate());

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.warn("Close loan failed: loan not found. loanId={}", loanId);
                    return new ResourceNotFoundException("Loan not found");
                });

        if (!loan.getUser().getId().equals(userId)) {
            log.warn("Close loan failed: access denied. loanId={}, requesterUserId={}, ownerUserId={}", loanId, userId, loan.getUser().getId());
            throw new AuthorizationException("Access Denied");
        }

        LocalDate startDate = loan.getStartDate();
        LocalDate endDate = loan.getEndDate();
        LocalDate closedDate = request.getClosedDate();

        if(closedDate.isBefore(startDate) || closedDate.isAfter(endDate)){
            log.warn(
                    "Close loan failed: invalid closed date. loanId={}, startDate={}, endDate={}, closedDate={}",
                    loanId, startDate, endDate, closedDate
            );
            throw new BusinessRuleException("Closed date must be within the loan period");
        }

        loan.setStatus(LoanStatus.CLOSED);
        loan.setClosedDate(request.getClosedDate());

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan closed successfully. loanId={}, userId={}, closedDate={}", loanId, userId, savedLoan.getClosedDate());

        return mapToLoanResponse(savedLoan);
    }

    @Override
    @Cacheable(cacheNames = CacheNames.LOAN_BY_ID, key = "#userId.toString() + ':' + #loanId.toString()")
    public LoanResponse getLoanById(UUID loanId, UUID userId) {
        log.info("Get loan by id request received. loanId={}, userId={}", loanId, userId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.warn("Get loan by id failed: loan not found. loanId={}", loanId);
                    return new ResourceNotFoundException("Loan not found");
                });

        if (!loan.getUser().getId().equals(userId)) {
            log.warn("Get loan by id failed: access denied. loanId={}, requesterUserId={}, ownerUserId={}", loanId, userId, loan.getUser().getId());
            throw new AuthorizationException("Access Denied");
        }
        log.info("Loan fetched successfully. loanId={}, userId={}", loanId, userId);

        return mapToLoanResponse(loan);
    }

    @Override
    @Cacheable(cacheNames = CacheNames.USER_LOANS, key = "#userId.toString() + ':' + #page + ':' + #size")
    public Page<LoanResponse> getUserLoans(UUID userId, int page, int size){
        log.info("Get user loans request received. userId={}, page={}, size={}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Loan> loansPage = loanRepository.findByUserId(userId, pageable);
        log.info(
                "User loans fetched successfully. userId={}, page={}, size={}, returnedElements={}, totalElements={}, totalPages={}",
                userId, page, size, loansPage.getNumberOfElements(), loansPage.getTotalElements(), loansPage.getTotalPages()
        );
        return loansPage.map(this::mapToLoanResponse);
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
