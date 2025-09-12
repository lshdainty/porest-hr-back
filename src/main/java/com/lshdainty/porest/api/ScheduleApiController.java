package com.lshdainty.porest.api;

import com.lshdainty.porest.domain.Schedule;
import com.lshdainty.porest.api.dto.ScheduleDto;
import com.lshdainty.porest.service.ScheduleService;
import com.lshdainty.porest.service.dto.ScheduleServiceDto;
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
    public ApiResponse registSchedule(@RequestBody ScheduleDto data, HttpServletRequest req) {
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

        return ApiResponse.success(ScheduleDto.builder().scheduleId(scheduleId).build());
    }

    @GetMapping("/api/v1/schedules/user/{userNo}")
    public ApiResponse getSchedulesByUser(@PathVariable("userNo") String userId) {
        List<Schedule> schedules = scheduleService.findSchedulesByUserId(userId);

        List<ScheduleDto> resp = schedules.stream()
                .map(s -> ScheduleDto
                        .builder()
                        .scheduleId(s.getId())
                        .scheduleType(s.getType())
                        .scheduleTypeName(s.getType().getStrName())
                        .scheduleDesc(s.getDesc())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .build()
                )
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/schedules/period")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse getSchedulesByPeriod(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        List<Schedule> schedules = scheduleService.findSchedulesByPeriod(startDate, endDate);

        List<ScheduleDto> resp = schedules.stream()
                .map(s -> ScheduleDto
                        .builder()
                        .scheduleId(s.getId())
                        .userId(s.getUser().getId())
                        .userName(s.getUser().getName())
                        .scheduleType(s.getType())
                        .scheduleTypeName(s.getType().getStrName())
                        .scheduleDesc(s.getDesc())
                        .startDate(s.getStartDate())
                        .endDate(s.getEndDate())
                        .build()
                )
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
