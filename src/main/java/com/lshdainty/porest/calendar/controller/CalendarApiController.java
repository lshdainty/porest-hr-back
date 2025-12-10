package com.lshdainty.porest.calendar.controller;

import com.lshdainty.porest.calendar.controller.dto.CalendarApiDto;
import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.common.type.DisplayType;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.service.ScheduleService;
import com.lshdainty.porest.vacation.service.VacationService;
import com.lshdainty.porest.vacation.service.dto.VacationServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
public class CalendarApiController implements CalendarApi {
    private final ScheduleService scheduleService;
    private final VacationService vacationService;
    private final MessageSource messageSource;

    @Override
    public ApiResponse searchEventsByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<CalendarApiDto.searchEventsByPeriodResp> resp = new ArrayList<>();

        List<Schedule> schedules = scheduleService.searchSchedulesByPeriod(startDate, endDate);
        resp.addAll(schedules.stream()
                .map(s -> new CalendarApiDto.searchEventsByPeriodResp(
                        s.getUser().getId(),
                        s.getUser().getName(),
                        getTranslatedName(s.getType()),
                        s.getType().name(),
                        s.getDesc(),
                        s.getStartDate(),
                        s.getEndDate(),
                        "schedule",
                        null,
                        s.getId()
                ))
                .toList());

        List<VacationServiceDto> histories = vacationService.getVacationUsagesByPeriod(startDate, endDate);
        resp.addAll(histories.stream()
                .map(v -> new CalendarApiDto.searchEventsByPeriodResp(
                        v.getUser().getId(),
                        v.getUser().getName(),
                        getTranslatedName(v.getTimeType()),
                        v.getTimeType().name(),
                        v.getDesc(),
                        v.getStartDate(),
                        v.getEndDate(),
                        "vacation",
                        v.getType(),
                        v.getId()
                ))
                .toList());

        return ApiResponse.success(resp);
    }

    private String getTranslatedName(DisplayType type) {
        if (type == null) return null;
        return messageSource.getMessage(type.getMessageKey(), null, LocaleContextHolder.getLocale());
    }
}
