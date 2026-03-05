package com.emiplanner.dto.loan;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoanCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String loanName;

    @NotBlank
    @Size(max = 50)
    private String providerName;

    @NotNull
    @Positive
    private BigDecimal emiAmount;

    @NotNull
    private LocalDate startDate;

    @NotNull
    @Positive
    private Integer tenureMonths;

}
