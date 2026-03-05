package com.emiplanner.dto.loan;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoanCloseRequest {

    @NotNull
    private LocalDate closedDate;

}
