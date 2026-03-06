package com.emiplanner.service;

import com.emiplanner.dto.calendar.MonthBreakdownResponse;
import com.emiplanner.dto.calendar.YearCalendarResponse;

import java.util.UUID;

public interface CalendarService {

    YearCalendarResponse getYearCalendar(UUID userId, int year);

    MonthBreakdownResponse getMonthBreakdown(UUID userId, int year, int month);

}