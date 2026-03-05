package com.emiplanner.dto.calendar;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MonthSummary {

    private Integer month;
    private Integer year;
    private BigDecimal totalEmiAmount;

}
