package com.lshdainty.porest.api;

import com.lshdainty.porest.domain.Schedule;
import com.lshdainty.porest.api.dto.CalendarDto;
import com.lshdainty.porest.service.ScheduleService;
import com.lshdainty.porest.service.VacationService;
import com.lshdainty.porest.service.dto.VacationServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CalendarApiController {
    private final ScheduleService scheduleService;
    private final VacationService vacationService;

    @GetMapping("/api/v1/calendar/period")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse getEventsByPeriod(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        List<CalendarDto> resp = new ArrayList<>();

        List<Schedule> schedules = scheduleService.findSchedulesByPeriod(startDate, endDate);
        resp.addAll(schedules.stream()
                .map(s -> CalendarDto
                        .builder()
                        .userId(s.getUser().getId())
                        .userName(s.getUser().getName())
                        .calendarName(s.getType().getStrName())
                        .calendarType(s.getType().name())
                        .calendarDesc(s.getDesc())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .domainType("schedule")
                        .historyIds(List.of())
                        .scheduleId(s.getId())
                        .build()
                )
                .toList());

        List<VacationServiceDto> histories = vacationService.getPeriodVacationUseHistories(startDate, endDate);
        resp.addAll(histories.stream()
                .map(v -> CalendarDto
                        .builder()
                        .userId(v.getUser().getId())
                        .userName(v.getUser().getName())
                        .calendarType(v.getTimeType().name())
                        .calendarName(v.getTimeType().getStrName())
                        .calendarDesc(v.getDesc())
                        .startDate(v.getStartDate())
                        .endDate(v.getEndDate())
                        .domainType("vacation")
                        .historyIds(v.getHistoryIds())
                        .scheduleId(0L)
                        .build()
                )
                .toList());

        return ApiResponse.success(resp);
    }
}
