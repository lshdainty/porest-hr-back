package com.lshdainty.myhr.api;

import com.lshdainty.myhr.domain.Schedule;
import com.lshdainty.myhr.dto.ScheduleDto;
import com.lshdainty.myhr.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ScheduleApiController {
    private final ScheduleService scheduleService;

    @PostMapping("/api/v1/schedule")
    public ApiResponse addSchedule(@RequestBody ScheduleDto scheduleDto, HttpServletRequest req) {
        Long scheduleId = null;

        if (scheduleDto.getScheduleType().isVacationType()) {
            scheduleId = scheduleService.addSchedule(
                    scheduleDto.getUserNo(),
                    scheduleDto.getVacationId(),
                    scheduleDto.getScheduleType(),
                    scheduleDto.getScheduleDesc(),
                    scheduleDto.getStartDate(),
                    scheduleDto.getEndDate(),
                    0L, // 추후 로그인한 유저의 id를 가져와서 여기에다 넣을 것
                    req.getRemoteAddr()
            );
        } else {
            scheduleId = scheduleService.addSchedule(
                    scheduleDto.getUserNo(),
                    scheduleDto.getScheduleType(),
                    scheduleDto.getScheduleDesc(),
                    scheduleDto.getStartDate(),
                    scheduleDto.getEndDate(),
                    0L, // 추후 로그인한 유저의 id를 가져와서 여기에다 넣을 것
                    req.getRemoteAddr()
            );
        }

        return ApiResponse.success(new ScheduleDto(scheduleId));
    }

    @GetMapping("/api/v1/schedules/user/{userNo}")
    public ApiResponse getSchedulesByUser(@PathVariable("userNo") Long userNo) {
        List<Schedule> schedules = scheduleService.findSchedulesByUserNo(userNo);

        List<ScheduleDto> resp = schedules.stream()
                .map(s -> new ScheduleDto(s))
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/schedules/period")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse getSchedulesByPeriod(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        List<Schedule> schedules = scheduleService.findSchedulesByPeriod(startDate, endDate);

        List<ScheduleDto> resp = schedules.stream()
                .map(s -> new ScheduleDto(s))
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @DeleteMapping("/api/v1/schedule/{id}")
    public ApiResponse deleteSchedule(@PathVariable("id") Long scheduleId, HttpServletRequest req) {
        Long delUserNo = 0L;   // 추후 로그인 한 사람의 id를 가져와서 삭제한 사람의 userNo에 세팅
        scheduleService.deleteSchedule(scheduleId, delUserNo, req.getRemoteAddr());
        return ApiResponse.success();
    }
}
