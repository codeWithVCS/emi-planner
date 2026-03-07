package com.emiplanner.controller;

import com.emiplanner.dto.loan.LoanCloseRequest;
import com.emiplanner.dto.loan.LoanCreateRequest;
import com.emiplanner.dto.loan.LoanResponse;
import com.emiplanner.dto.loan.LoanUpdateRequest;
import com.emiplanner.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> addLoan(@Valid @RequestBody LoanCreateRequest request){
        UUID userId = getCurrentUserId();
        LoanResponse response = loanService.createLoan(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<LoanResponse>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        UUID userId = getCurrentUserId();
        Page<LoanResponse> responses = loanService.getUserLoans(userId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable UUID loanId){
        UUID userId = getCurrentUserId();
        LoanResponse response = loanService.getLoanById(loanId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{loanId}")
    public ResponseEntity<LoanResponse> updateLoan(@PathVariable UUID loanId, @Valid @RequestBody LoanUpdateRequest request){
        UUID userId = getCurrentUserId();
        LoanResponse response = loanService.updateLoan(loanId, userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<Void> deleteLoan(@PathVariable UUID loanId){
        UUID userId = getCurrentUserId();
        loanService.deleteLoan(loanId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{loanId}/close")
    public ResponseEntity<LoanResponse> closeLoanEarly(@PathVariable UUID loanId, @Valid @RequestBody LoanCloseRequest request){
        UUID userId = getCurrentUserId();
        LoanResponse response = loanService.closeLoan(loanId, userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private UUID getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

}
