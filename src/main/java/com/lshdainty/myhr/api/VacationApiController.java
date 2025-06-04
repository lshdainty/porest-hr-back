package com.lshdainty.myhr.api;

import com.lshdainty.myhr.domain.User;
import com.lshdainty.myhr.domain.Vacation;
import com.lshdainty.myhr.dto.UserDto;
import com.lshdainty.myhr.dto.VacationDto;
import com.lshdainty.myhr.service.VacationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VacationApiController {
    private final VacationService vacationService;

    @PostMapping("/api/v1/vacation")
    public ApiResponse registVacation(@RequestBody VacationDto vacationDto, HttpServletRequest req) {
        Long vacationId = vacationService.registVacation(
                vacationDto.getUserNo(),
                vacationDto.getVacationDesc(),
                vacationDto.getVacationType(),
                vacationDto.getGrantTime(),
                vacationDto.getOccurDate(),
                vacationDto.getExpiryDate(),
                0L, // 추후 로그인한 유저의 id를 가져와서 여기에다 넣을 것
                req.getRemoteAddr()
        );

        return ApiResponse.success(VacationDto.builder().vacationId(vacationId).build());
    }

    @PostMapping("/api/v1/vacation/use/{vacationId}")
    public ApiResponse useVacation(@PathVariable("vacationId") Long vacationId, @RequestBody VacationDto vacationDto, HttpServletRequest req) {
        Long respVacationId = vacationService.useVacation(
                vacationDto.getUserNo(),
                vacationId,
                vacationDto.getVacationDesc(),
                vacationDto.getVacationTimeType(),
                vacationDto.getStartDate(),
                vacationDto.getEndDate(),
                0L, // 추후 로그인한 유저의 id를 가져와서 여기에다 넣을 것
                req.getRemoteAddr()
        );

        return ApiResponse.success(VacationDto.builder().vacationId(respVacationId).build());
    }

    @GetMapping("/api/v1/vacations/user/{userNo}")
    public ApiResponse getVacationsByUser(@PathVariable("userNo") Long userNo) {
        List<Vacation> vacations = vacationService.findVacationsByUser(userNo);

        List<VacationDto> resp = vacations.stream()
                .map(v -> VacationDto
                        .builder()
                        .vacationId(v.getId())
                        .vacationType(v.getType())
                        .vacationTypeName(v.getType().getStrName())
                        .remainTime(v.getRemainTime())
                        .occurDate(v.getOccurDate())
                        .expiryDate(v.getExpiryDate())
                        .build())
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/vacations/usergroup")
    public ApiResponse getVacationsByUserGroup() {
        List<User> usersVacations = vacationService.findVacationsByUserGroup();

        List<UserDto> resp = new ArrayList<>();
        for (User user : usersVacations) {
            List<VacationDto> vacations = user.getVacations().stream()
                    .map(v -> VacationDto
                            .builder()
                            .vacationId(v.getId())
                            .vacationType(v.getType())
                            .vacationTypeName(v.getType().getStrName())
                            .remainTime(v.getRemainTime())
                            .occurDate(v.getOccurDate())
                            .expiryDate(v.getExpiryDate())
                            .build())
                    .toList();

            resp.add(UserDto
                    .builder()
                    .userNo(user.getId())
                    .userName(user.getName())
                    .vacations(vacations)
                    .build()
            );
        }

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/vacation/available/{userNo}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse getAvailableVacation(@PathVariable("userNo") Long userNo, @RequestParam("startDate") LocalDateTime startDate, HttpServletRequest req) {
        List<Vacation> vacations = vacationService.getAvailableVacation(userNo, startDate);

        List<VacationDto> resp = vacations.stream()
                .map(v -> VacationDto
                        .builder()
                        .vacationId(v.getId())
                        .vacationType(v.getType())
                        .vacationTypeName(v.getType().getStrName())
                        .remainTime(v.getRemainTime())
                        .occurDate(v.getOccurDate())
                        .expiryDate(v.getExpiryDate())
                        .build()
                )
                .toList();

        return ApiResponse.success(resp);
    }

    @DeleteMapping("/api/v1/vacation/history/{id}")
    public ApiResponse deleteVacationHistory(@PathVariable("id") Long vacationHistoryId, HttpServletRequest req) {
        Long delUserNo = 0L;   // 추후 로그인 한 사람의 id를 가져와서 삭제한 사람의 userNo에 세팅
        vacationService.deleteVacationHistory(vacationHistoryId, delUserNo, req.getRemoteAddr());
        return ApiResponse.success();
    }
}
