package com.lshdainty.porest.schedule.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.controller.dto.ScheduleApiDto;
import com.lshdainty.porest.schedule.service.ScheduleService;
import com.lshdainty.porest.schedule.service.dto.ScheduleServiceDto;
import com.lshdainty.porest.common.type.DisplayType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScheduleApiController implements ScheduleApi {
    private final ScheduleService scheduleService;
    private final MessageSource messageSource;

    @Override
    @PreAuthorize("hasAuthority('SCHEDULE_CREATE')")
    public ApiResponse registSchedule(ScheduleApiDto.RegistScheduleReq data, HttpServletRequest req) {
        Long scheduleId = scheduleService.registSchedule(ScheduleServiceDto.builder()
                .userId(data.getUserId())
                .type(data.getScheduleType())
                .desc(data.getScheduleDesc())
                .startDate(data.getStartDate())
                .endDate(data.getEndDate())
                .build()
        );

        return ApiResponse.success(new ScheduleApiDto.RegistScheduleResp(scheduleId));
    }

    @Override
    @PreAuthorize("hasAuthority('SCHEDULE_UPDATE')")
    public ApiResponse updateSchedule(Long scheduleId, ScheduleApiDto.UpdateScheduleReq data) {
        Long newScheduleId = scheduleService.updateSchedule(
                scheduleId,
                ScheduleServiceDto.builder()
                        .userId(data.getUserId())
                        .type(data.getScheduleType())
                        .desc(data.getScheduleDesc())
                        .startDate(data.getStartDate())
                        .endDate(data.getEndDate())
                        .build()
        );

        return ApiResponse.success(new ScheduleApiDto.UpdateScheduleResp(newScheduleId));
    }

    @Override
    @PreAuthorize("hasAuthority('SCHEDULE_DELETE')")
    public ApiResponse deleteSchedule(Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    public ApiResponse searchSchedulesByUser(String userId) {
        List<Schedule> schedules = scheduleService.searchSchedulesByUser(userId);

        List<ScheduleApiDto.SearchSchedulesByUserResp> resp = schedules.stream()
                .map(s -> new ScheduleApiDto.SearchSchedulesByUserResp(
                        s.getId(),
                        s.getType(),
                        getTranslatedName(s.getType()),
                        s.getDesc(),
                        s.getStartDate(),
                        s.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    public ApiResponse searchSchedulesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<Schedule> schedules = scheduleService.searchSchedulesByPeriod(startDate, endDate);

        List<ScheduleApiDto.SearchSchedulesByPeriodResp> resp = schedules.stream()
                .map(s -> new ScheduleApiDto.SearchSchedulesByPeriodResp(
                        s.getId(),
                        s.getUser().getId(),
                        s.getUser().getName(),
                        s.getType(),
                        getTranslatedName(s.getType()),
                        s.getDesc(),
                        s.getStartDate(),
                        s.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    private String getTranslatedName(DisplayType type) {
        if (type == null) return null;
        return messageSource.getMessage(type.getMessageKey(), null, LocaleContextHolder.getLocale());
    }
}
