package com.lshdainty.porest.vacation.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.controller.dto.VacationApiDto;
import com.lshdainty.porest.vacation.domain.Vacation;
import com.lshdainty.porest.vacation.service.VacationService;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.service.dto.VacationServiceDto;
import com.lshdainty.porest.vacation.type.VacationTimeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VacationApiController {
    private final VacationService vacationService;

    @PostMapping("/api/v1/vacation")
    public ApiResponse registVacation(@RequestBody VacationApiDto.RegistVacationReq data) {
        Long vacationId = vacationService.registVacation(VacationServiceDto.builder()
                        .userId(data.getUserId())
                        .desc(data.getVacationDesc())
                        .type(data.getVacationType())
                        .grantTime(data.getGrantTime())
                        .occurDate(data.getOccurDate())
                        .expiryDate(data.getExpiryDate())
                        .build()
        );

        return ApiResponse.success(new VacationApiDto.RegistVacationResp(vacationId));
    }

    @PostMapping("/api/v1/vacation/use")
    public ApiResponse useVacation(@RequestBody VacationApiDto.UseVacationReq data) {
        Long vacationUsageId = vacationService.useVacation(VacationServiceDto.builder()
                        .userId(data.getUserId())
                        .vacationType(data.getVacationType())
                        .desc(data.getVacationDesc())
                        .timeType(data.getVacationTimeType())
                        .startDate(data.getStartDate())
                        .endDate(data.getEndDate())
                        .build()
        );

        return ApiResponse.success(new VacationApiDto.UseVacationResp(vacationUsageId));
    }

    @GetMapping("/api/v1/vacations/user/{userId}")
    public ApiResponse searchUserVacations(@PathVariable("userId") String userId) {
        List<Vacation> vacations = vacationService.searchUserVacations(userId);

        List<VacationApiDto.SearchUserVacationsResp> resp = vacations.stream()
                .map(v -> new VacationApiDto.SearchUserVacationsResp(
                        v.getId(),
                        v.getType(),
                        v.getType().getViewName(),
                        v.getRemainTime(),
                        v.getOccurDate(),
                        v.getExpiryDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/vacations/usergroup")
    public ApiResponse searchUserGroupVacations() {
        List<User> usersVacations = vacationService.searchUserGroupVacations();

        List<VacationApiDto.SearchUserGroupVacationsResp> resp = usersVacations.stream()
                .map(user -> {
                    List<VacationApiDto.SearchUserGroupVacationsResp.VacationInfo> vacations =
                            user.getVacations().stream()
                                    .map(v -> new VacationApiDto.SearchUserGroupVacationsResp.VacationInfo(
                                            v.getId(),
                                            v.getType(),
                                            v.getType().getViewName(),
                                            v.getRemainTime(),
                                            v.getOccurDate(),
                                            v.getExpiryDate()
                                    ))
                                    .toList();

                    return new VacationApiDto.SearchUserGroupVacationsResp(
                            user.getId(),
                            user.getName(),
                            vacations
                    );
                })
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/vacation/available/{userId}")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse searcgAvailableVacations(@PathVariable("userId") String userId,
                                                @RequestParam("startDate") LocalDateTime startDate) {
        List<Vacation> vacations = vacationService.searcgAvailableVacations(userId, startDate);

        List<VacationApiDto.SearchAvailableVacationsResp> resp = vacations.stream()
                .map(v -> new VacationApiDto.SearchAvailableVacationsResp(
                        v.getId(),
                        v.getType(),
                        v.getType().getViewName(),
                        v.getRemainTime(),
                        v.getOccurDate(),
                        v.getExpiryDate(),
                        VacationTimeType.convertValueToDay(v.getRemainTime())
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @DeleteMapping("/api/v1/vacation/history/{id}")
    public ApiResponse deleteVacationHistory(@PathVariable("id") Long vacationHistoryId) {
        String delUserId = "";   // 추후 로그인 한 사람의 id를 가져와서 삭제한 사람의 userNo에 세팅
        vacationService.deleteVacationHistory(vacationHistoryId);
        return ApiResponse.success();
    }

    @GetMapping("/api/v1/vacation/use/histories/period")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse searchPeriodVacationUseHistories(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        List<VacationServiceDto> histories = vacationService.searchPeriodVacationUseHistories(startDate, endDate);

        List<VacationApiDto.SearchPeriodVacationUseHistoriesResp> resp = histories.stream()
                .map(v -> new VacationApiDto.SearchPeriodVacationUseHistoriesResp(
                        v.getUser().getId(),
                        v.getUser().getName(),
                        v.getId(),
                        v.getDesc(),
                        v.getHistoryIds(),
                        v.getTimeType(),
                        v.getTimeType().getStrName(),
                        v.getStartDate(),
                        v.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/vacation/use/histories/user/period")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse searchUserPeriodVacationUseHistories(
            @RequestParam("userId") String userId,
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        List<VacationServiceDto> histories = vacationService.searchUserPeriodVacationUseHistories(userId, startDate, endDate);

        List<VacationApiDto.SearchUserPeriodVacationUseHistoriesResp> resp = histories.stream()
                .map(v -> new VacationApiDto.SearchUserPeriodVacationUseHistoriesResp(
                        v.getId(),
                        v.getDesc(),
                        v.getHistoryId(),
                        v.getTimeType(),
                        v.getTimeType().getStrName(),
                        v.getStartDate(),
                        v.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/vacation/use/histories/user/month/stats")
    public ApiResponse searchUserMonthStatsVacationUseHistories(
            @RequestParam("userId") String userId,
            @RequestParam("year") String year) {
        List<VacationServiceDto> histories = vacationService.searchUserMonthStatsVacationUseHistories(userId, year);

        List<VacationApiDto.SearchUserMonthStatsVacationUseHistoriesResp> resp = histories.stream()
                .map(v -> new VacationApiDto.SearchUserMonthStatsVacationUseHistoriesResp(
                        v.getMonth(),
                        v.getUsedTime(),
                        VacationTimeType.convertValueToDay(v.getUsedTime())
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @GetMapping("/api/v1/vacation/use/stats/user")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse searchUserVacationUseStats(
            @RequestParam("userId") String userId,
            @RequestParam("baseDate") LocalDateTime baseDate) {
        VacationServiceDto stats = vacationService.searchUserVacationUseStats(userId, baseDate);

        return ApiResponse.success(new VacationApiDto.SearchUserVacationUseStatsResp(
                stats.getRemainTime(),
                VacationTimeType.convertValueToDay(stats.getRemainTime()),
                stats.getUsedTime(),
                VacationTimeType.convertValueToDay(stats.getUsedTime()),
                stats.getExpectUsedTime(),
                VacationTimeType.convertValueToDay(stats.getExpectUsedTime()),
                stats.getPrevRemainTime(),
                VacationTimeType.convertValueToDay(stats.getPrevRemainTime()),
                stats.getPrevUsedTime(),
                VacationTimeType.convertValueToDay(stats.getPrevUsedTime()),
                stats.getPrevExpectUsedTime(),
                VacationTimeType.convertValueToDay(stats.getPrevExpectUsedTime()),
                stats.getRemainTime().subtract(stats.getPrevRemainTime()),
                VacationTimeType.convertValueToDay(stats.getRemainTime().subtract(stats.getPrevRemainTime()).abs()),
                stats.getUsedTime().subtract(stats.getPrevUsedTime()),
                VacationTimeType.convertValueToDay(stats.getUsedTime().subtract(stats.getPrevUsedTime()).abs())
        ));
    }

    @PostMapping("/api/v1/vacation/policies")
    public ApiResponse registVacationPolicy(@RequestBody VacationApiDto.RegistVacationPolicyReq data) {
        Long vacationPolicyId = vacationService.registVacationPolicy(VacationPolicyServiceDto.builder()
                .name(data.getVacationPolicyName())
                .desc(data.getVacationPolicyDesc())
                .vacationType(data.getVacationType())
                .grantMethod(data.getGrantMethod())
                .grantTime(data.getGrantTime())
                .repeatUnit(data.getRepeatUnit())
                .repeatInterval(data.getRepeatInterval())
                .specificMonths(data.getSpecificMonths())
                .specificDays(data.getSpecificDays())
                .firstGrantDate(data.getFirstGrantDate())
                .isRecurring(data.getIsRecurring())
                .maxGrantCount(data.getMaxGrantCount())
                .build()
        );

        return ApiResponse.success(new VacationApiDto.RegistVacationPolicyResp(vacationPolicyId));
    }

    @GetMapping("/api/v1/vacation/policies/{id}")
    public ApiResponse searchVacationPolicy(@PathVariable("id") Long vacationPolicyId) {
        VacationPolicyServiceDto policy = vacationService.searchVacationPolicy(vacationPolicyId);

        return ApiResponse.success(new VacationApiDto.SearchVacationPoliciesResp(
                policy.getId(),
                policy.getName(),
                policy.getDesc(),
                policy.getVacationType(),
                policy.getGrantMethod(),
                policy.getGrantTime(),
                VacationTimeType.convertValueToDay(policy.getGrantTime()),
                policy.getRepeatUnit(),
                policy.getRepeatInterval(),
                policy.getSpecificMonths(),
                policy.getSpecificDays()
        ));
    }

    @GetMapping("/api/v1/vacation/policies")
    public ApiResponse searchVacationPolicies() {
        List<VacationPolicyServiceDto> policies = vacationService.searchVacationPolicies();

        List<VacationApiDto.SearchVacationPoliciesResp> resp = policies.stream()
                .map(vp -> new VacationApiDto.SearchVacationPoliciesResp(
                        vp.getId(),
                        vp.getName(),
                        vp.getDesc(),
                        vp.getVacationType(),
                        vp.getGrantMethod(),
                        vp.getGrantTime(),
                        VacationTimeType.convertValueToDay(vp.getGrantTime()),
                        vp.getRepeatUnit(),
                        vp.getRepeatInterval(),
                        vp.getSpecificMonths(),
                        vp.getSpecificDays()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    /**
     * 휴가 정책 삭제
     * DELETE /api/v1/vacation/policies/{vacationPolicyId}
     */
    @DeleteMapping("/api/v1/vacation/policies/{vacationPolicyId}")
    public ApiResponse deleteVacationPolicy(@PathVariable("vacationPolicyId") Long vacationPolicyId) {
        Long deletedPolicyId = vacationService.deleteVacationPolicy(vacationPolicyId);

        return ApiResponse.success(new VacationApiDto.DeleteVacationPolicyResp(deletedPolicyId));
    }

    /**
     * 유저에게 여러 휴가 정책을 일괄 할당
     * POST /api/v1/users/{userId}/vacation-policies
     */
    @PostMapping("/api/v1/users/{userId}/vacation-policies")
    public ApiResponse assignVacationPoliciesToUser(
            @PathVariable("userId") String userId,
            @RequestBody VacationApiDto.AssignVacationPoliciesToUserReq data) {
        List<Long> assignedPolicyIds = vacationService.assignVacationPoliciesToUser(userId, data.getVacationPolicyIds());

        return ApiResponse.success(new VacationApiDto.AssignVacationPoliciesToUserResp(
                userId,
                assignedPolicyIds
        ));
    }

    /**
     * 유저에게 할당된 휴가 정책 조회
     * GET /api/v1/users/{userId}/vacation-policies
     */
    @GetMapping("/api/v1/users/{userId}/vacation-policies")
    public ApiResponse searchUserVacationPolicies(@PathVariable("userId") String userId) {
        List<VacationPolicyServiceDto> policies = vacationService.searchUserVacationPolicies(userId);

        List<VacationApiDto.SearchUserVacationPoliciesResp> resp = policies.stream()
                .map(vp -> new VacationApiDto.SearchUserVacationPoliciesResp(
                        vp.getUserVacationPolicyId(),
                        vp.getId(),
                        vp.getName(),
                        vp.getDesc(),
                        vp.getVacationType(),
                        vp.getGrantMethod(),
                        vp.getGrantTime(),
                        VacationTimeType.convertValueToDay(vp.getGrantTime()),
                        vp.getRepeatUnit(),
                        vp.getRepeatInterval(),
                        vp.getSpecificMonths(),
                        vp.getSpecificDays()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    /**
     * 유저에게 부여된 휴가 정책 회수 (단일)
     * DELETE /api/v1/users/{userId}/vacation-policies/{vacationPolicyId}
     */
    @DeleteMapping("/api/v1/users/{userId}/vacation-policies/{vacationPolicyId}")
    public ApiResponse revokeVacationPolicyFromUser(
            @PathVariable("userId") String userId,
            @PathVariable("vacationPolicyId") Long vacationPolicyId) {
        Long userVacationPolicyId = vacationService.revokeVacationPolicyFromUser(userId, vacationPolicyId);

        return ApiResponse.success(new VacationApiDto.RevokeVacationPolicyFromUserResp(
                userId,
                vacationPolicyId,
                userVacationPolicyId
        ));
    }

    /**
     * 유저에게 부여된 여러 휴가 정책 일괄 회수
     * DELETE /api/v1/users/{userId}/vacation-policies
     */
    @DeleteMapping("/api/v1/users/{userId}/vacation-policies")
    public ApiResponse revokeVacationPoliciesFromUser(
            @PathVariable("userId") String userId,
            @RequestBody VacationApiDto.RevokeVacationPoliciesFromUserReq data) {
        List<Long> revokedPolicyIds = vacationService.revokeVacationPoliciesFromUser(userId, data.getVacationPolicyIds());

        return ApiResponse.success(new VacationApiDto.RevokeVacationPoliciesFromUserResp(
                userId,
                revokedPolicyIds
        ));
    }
}
