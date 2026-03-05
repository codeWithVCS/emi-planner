package com.emiplanner.dto.loan;

import com.emiplanner.entity.LoanStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoanResponse {

    private UUID id;
    private String loanName;
    private String providerName;
    private BigDecimal emiAmount;
    private LocalDate startDate;
    private Integer tenureMonths;
    private LocalDate endDate;
    private LocalDate closedDate;
    private LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
