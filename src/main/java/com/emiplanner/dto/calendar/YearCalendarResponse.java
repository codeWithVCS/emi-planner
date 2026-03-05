package com.emiplanner.dto.calendar;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class YearCalendarResponse {

    private Integer year;

    @Builder.Default
    private List<MonthSummary> months = new ArrayList<>();

}
