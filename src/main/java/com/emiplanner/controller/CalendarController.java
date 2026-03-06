package com.emiplanner.controller;

import com.emiplanner.dto.calendar.MonthBreakdownResponse;
import com.emiplanner.dto.calendar.YearCalendarResponse;
import com.emiplanner.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/{year}")
    public ResponseEntity<YearCalendarResponse> getYearCalendar(@PathVariable int year){
        UUID userId = getCurrentUserId();
        YearCalendarResponse response = calendarService.getYearCalendar(userId, year);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{year}/{month}")
    public ResponseEntity<MonthBreakdownResponse> getMonthBreakdown(@PathVariable int year, @PathVariable int month){
        UUID userId = getCurrentUserId();
        MonthBreakdownResponse response = calendarService.getMonthBreakdown(userId, year, month);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private UUID getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) authentication.getPrincipal();
    }

}
