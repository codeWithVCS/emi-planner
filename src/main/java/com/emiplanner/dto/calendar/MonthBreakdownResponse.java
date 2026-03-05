package com.emiplanner.dto.calendar;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MonthBreakdownResponse {

    private Integer month;
    private Integer year;
    private BigDecimal totalEmiAmount;

    @Builder.Default
    private List<LoanContribution> loans = new ArrayList<>();

}
