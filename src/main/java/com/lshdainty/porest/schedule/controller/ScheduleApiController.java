package com.lshdainty.porest.schedule.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.schedule.domain.Schedule;
import com.lshdainty.porest.schedule.controller.dto.ScheduleApiDto;
import com.lshdainty.porest.schedule.service.ScheduleService;
import com.lshdainty.porest.schedule.service.dto.ScheduleServiceDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScheduleApiController {
    private final ScheduleService scheduleService;

    @PostMapping("/api/v1/schedule")
    @PreAuthorize("hasAuthority('SCHEDULE_CREATE')")
    public ApiResponse registSchedule(@RequestBody ScheduleApiDto.RegistScheduleReq data, HttpServletRequest req) {
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

    @PutMapping("/api/v1/schedule/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_UPDATE')")
    public ApiResponse updateSchedule(
            @PathVariable("id") Long scheduleId,
            @RequestBody ScheduleApiDto.UpdateScheduleReq data) {
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

    @DeleteMapping("/api/v1/schedule/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_DELETE')")
    public ApiResponse deleteSchedule(@PathVariable("id") Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ApiResponse.success();
    }

    @GetMapping("/api/v1/schedules/user/{userNo}")
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    public ApiResponse searchSchedulesByUser(@PathVariable("userNo") String userId) {
        List<Schedule> schedules = scheduleService.searchSchedulesByUser(userId);

        List<ScheduleApiDto.SearchSchedulesByUserResp> resp = schedules.stream()
                .map(s -> new ScheduleApiDto.SearchSchedulesByUserResp(
                        s.getId(),
                        s.getType(),
                        s.getType().getViewName(),
                        s.getDesc(),
                        s.getStartDate(),
                        s.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/schedules/period")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @PreAuthorize("hasAuthority('SCHEDULE_READ')")
    public ApiResponse searchSchedulesByPeriod(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        List<Schedule> schedules = scheduleService.searchSchedulesByPeriod(startDate, endDate);

        List<ScheduleApiDto.SearchSchedulesByPeriodResp> resp = schedules.stream()
                .map(s -> new ScheduleApiDto.SearchSchedulesByPeriodResp(
                        s.getId(),
                        s.getUser().getId(),
                        s.getUser().getName(),
                        s.getType(),
                        s.getType().getViewName(),
                        s.getDesc(),
                        s.getStartDate(),
                        s.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }
}
