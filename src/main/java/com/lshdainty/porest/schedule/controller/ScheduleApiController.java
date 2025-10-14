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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScheduleApiController {
    private final ScheduleService scheduleService;

    @PostMapping("/api/v1/schedule")
    public ApiResponse registSchedule(@RequestBody ScheduleApiDto.RegistScheduleReq data, HttpServletRequest req) {
        Long scheduleId = scheduleService.registSchedule(ScheduleServiceDto.builder()
                .userId(data.getUserId())
                .type(data.getScheduleType())
                .desc(data.getScheduleDesc())
                .startDate(data.getStartDate())
                .endDate(data.getEndDate())
                .build(),
                "", // 추후 로그인한 유저의 id를 가져와서 여기에다 넣을 것
                req.getRemoteAddr()
        );

        return ApiResponse.success(new ScheduleApiDto.RegistScheduleResp(scheduleId));
    }

    @GetMapping("/api/v1/schedules/user/{userNo}")
    public ApiResponse searchSchedulesByUser(@PathVariable("userNo") String userId) {
        List<Schedule> schedules = scheduleService.searchSchedulesByUser(userId);

        List<ScheduleApiDto.SearchSchedulesByUserResp> resp = schedules.stream()
                .map(s -> new ScheduleApiDto.SearchSchedulesByUserResp(
                        s.getId(),
                        s.getType(),
                        s.getType().getStrName(),
                        s.getDesc(),
                        s.getStartDate(),
                        s.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/schedules/period")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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
                        s.getType().getStrName(),
                        s.getDesc(),
                        s.getStartDate(),
                        s.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @DeleteMapping("/api/v1/schedule/{id}")
    public ApiResponse deleteSchedule(@PathVariable("id") Long scheduleId, HttpServletRequest req) {
        String delUserId = "";   // 추후 로그인 한 사람의 id를 가져와서 삭제한 사람의 userNo에 세팅
        scheduleService.deleteSchedule(scheduleId, delUserId, req.getRemoteAddr());
        return ApiResponse.success();
    }
}
