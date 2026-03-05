package com.emiplanner.dto.calendar;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoanContribution {

    private UUID loanId;
    private String loanName;
    private BigDecimal emiAmount;

}
