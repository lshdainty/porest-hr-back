package com.lshdainty.porest.vacation.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.vacation.controller.dto.VacationApiDto;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.service.VacationService;
import com.lshdainty.porest.vacation.service.dto.VacationApprovalServiceDto;
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

    /**
     * 휴가 사용
     * POST /api/v1/vacation-usages
     */
    @PostMapping("/api/v1/vacation-usages")
    public ApiResponse useVacation(@RequestBody VacationApiDto.UseVacationReq data) {
        Long vacationUsageId = vacationService.useVacation(VacationServiceDto.builder()
                        .userId(data.getUserId())
                        .type(data.getVacationType())
                        .desc(data.getVacationDesc())
                        .timeType(data.getVacationTimeType())
                        .startDate(data.getStartDate())
                        .endDate(data.getEndDate())
                        .build()
        );

        return ApiResponse.success(new VacationApiDto.UseVacationResp(vacationUsageId));
    }

    /**
     * 특정 유저의 휴가 정보 조회 (부여/사용 내역)
     * GET /api/v1/users/{userId}/vacations
     */
    @GetMapping("/api/v1/users/{userId}/vacations")
    public ApiResponse getUserVacationHistory(@PathVariable("userId") String userId) {
        VacationServiceDto vacationInfo = vacationService.getUserVacationHistory(userId);

        // VacationGrant 정보 변환
        List<VacationApiDto.GetUserVacationHistoryResp.VacationGrantInfo> grantInfos = vacationInfo.getGrants().stream()
                .map(g -> new VacationApiDto.GetUserVacationHistoryResp.VacationGrantInfo(
                        g.getId(),
                        g.getType(),
                        g.getType().getViewName(),
                        g.getDesc(),
                        g.getGrantTime(),
                        g.getRemainTime(),
                        g.getGrantDate(),
                        g.getExpiryDate()
                ))
                .toList();

        // VacationUsage 정보 변환
        List<VacationApiDto.GetUserVacationHistoryResp.VacationUsageInfo> usageInfos = vacationInfo.getUsages().stream()
                .map(u -> new VacationApiDto.GetUserVacationHistoryResp.VacationUsageInfo(
                        u.getId(),
                        u.getDesc(),
                        u.getType(),
                        u.getType().getStrName(),
                        u.getUsedTime(),
                        u.getStartDate(),
                        u.getEndDate()
                ))
                .toList();

        return ApiResponse.success(new VacationApiDto.GetUserVacationHistoryResp(grantInfos, usageInfos));
    }

    /**
     * 모든 유저의 휴가 정보 조회
     * GET /api/v1/vacations
     */
    @GetMapping("/api/v1/vacations")
    public ApiResponse getAllUsersVacationHistory() {
        List<VacationServiceDto> usersVacations = vacationService.getAllUsersVacationHistory();

        List<VacationApiDto.GetAllUsersVacationHistoryResp> resp = usersVacations.stream()
                .map(dto -> {
                    // VacationGrant 정보 변환
                    List<VacationApiDto.GetAllUsersVacationHistoryResp.VacationGrantInfo> grantInfos =
                            dto.getGrants().stream()
                                    .map(g -> new VacationApiDto.GetAllUsersVacationHistoryResp.VacationGrantInfo(
                                            g.getId(),
                                            g.getType(),
                                            g.getType().getViewName(),
                                            g.getDesc(),
                                            g.getGrantTime(),
                                            g.getRemainTime(),
                                            g.getGrantDate(),
                                            g.getExpiryDate()
                                    ))
                                    .toList();

                    // VacationUsage 정보 변환
                    List<VacationApiDto.GetAllUsersVacationHistoryResp.VacationUsageInfo> usageInfos =
                            dto.getUsages().stream()
                                    .map(u -> new VacationApiDto.GetAllUsersVacationHistoryResp.VacationUsageInfo(
                                            u.getId(),
                                            u.getDesc(),
                                            u.getType(),
                                            u.getType().getStrName(),
                                            u.getUsedTime(),
                                            u.getStartDate(),
                                            u.getEndDate()
                                    ))
                                    .toList();

                    return new VacationApiDto.GetAllUsersVacationHistoryResp(
                            dto.getUser().getId(),
                            dto.getUser().getName(),
                            grantInfos,
                            usageInfos
                    );
                })
                .toList();

        return ApiResponse.success(resp);
    }

    /**
     * 특정 유저의 사용 가능한 휴가 조회
     * GET /api/v1/users/{userId}/vacations/available
     */
    @GetMapping("/api/v1/users/{userId}/vacations/available")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse getAvailableVacations(@PathVariable("userId") String userId,
                                                @RequestParam("startDate") LocalDateTime startDate) {
        List<VacationServiceDto> availableVacations = vacationService.getAvailableVacations(userId, startDate);

        List<VacationApiDto.GetAvailableVacationsResp> resp = availableVacations.stream()
                .map(dto -> new VacationApiDto.GetAvailableVacationsResp(
                        dto.getType(),
                        dto.getType().getViewName(),
                        dto.getRemainTime(),
                        VacationTimeType.convertValueToDay(dto.getRemainTime())
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    /**
     * 휴가 사용 내역 삭제
     * DELETE /api/v1/vacation-usages/{id}
     */
    @DeleteMapping("/api/v1/vacation-usages/{id}")
    public ApiResponse cancelVacationUsage(@PathVariable("id") Long vacationUsageId) {
        vacationService.cancelVacationUsage(vacationUsageId);
        return ApiResponse.success();
    }

    /**
     * 기간별 휴가 사용 내역 조회 (전체 유저)
     * GET /api/v1/vacation-usages
     */
    @GetMapping("/api/v1/vacation-usages")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse getVacationUsagesByPeriod(
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        List<VacationServiceDto> histories = vacationService.getVacationUsagesByPeriod(startDate, endDate);

        List<VacationApiDto.GetVacationUsagesByPeriodResp> resp = histories.stream()
                .map(dto -> new VacationApiDto.GetVacationUsagesByPeriodResp(
                        dto.getUser().getId(),
                        dto.getUser().getName(),
                        dto.getId(),
                        dto.getDesc(),
                        dto.getTimeType(),
                        dto.getTimeType().getStrName(),
                        dto.getUsedTime(),
                        dto.getStartDate(),
                        dto.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    /**
     * 특정 유저의 기간별 휴가 사용 내역 조회
     * GET /api/v1/users/{userId}/vacation-usages
     */
    @GetMapping("/api/v1/users/{userId}/vacation-usages")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse getUserVacationUsagesByPeriod(
            @PathVariable("userId") String userId,
            @RequestParam("startDate") LocalDateTime startDate,
            @RequestParam("endDate") LocalDateTime endDate) {
        List<VacationServiceDto> histories = vacationService.getUserVacationUsagesByPeriod(userId, startDate, endDate);

        List<VacationApiDto.GetUserVacationUsagesByPeriodResp> resp = histories.stream()
                .map(dto -> new VacationApiDto.GetUserVacationUsagesByPeriodResp(
                        dto.getId(),
                        dto.getDesc(),
                        dto.getTimeType(),
                        dto.getTimeType().getStrName(),
                        dto.getUsedTime(),
                        dto.getStartDate(),
                        dto.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    /**
     * 특정 유저의 월별 휴가 사용 통계 조회
     * GET /api/v1/users/{userId}/vacation-usages/monthly-stats
     */
    @GetMapping("/api/v1/users/{userId}/vacation-usages/monthly-stats")
    public ApiResponse getUserMonthlyVacationStats(
            @PathVariable("userId") String userId,
            @RequestParam("year") String year) {
        List<VacationServiceDto> histories = vacationService.getUserMonthlyVacationStats(userId, year);

        List<VacationApiDto.GetUserMonthlyVacationStatsResp> resp = histories.stream()
                .map(v -> new VacationApiDto.GetUserMonthlyVacationStatsResp(
                        v.getMonth(),
                        v.getUsedTime(),
                        VacationTimeType.convertValueToDay(v.getUsedTime())
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    /**
     * 특정 유저의 휴가 사용 통계 조회
     * GET /api/v1/users/{userId}/vacations/stats
     */
    @GetMapping("/api/v1/users/{userId}/vacations/stats")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public ApiResponse getUserVacationStats(
            @PathVariable("userId") String userId,
            @RequestParam("baseDate") LocalDateTime baseDate) {
        VacationServiceDto stats = vacationService.getUserVacationStats(userId, baseDate);

        return ApiResponse.success(new VacationApiDto.GetUserVacationStatsResp(
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

    /**
     * 휴가 정책 등록
     * POST /api/v1/vacation-policies
     */
    @PostMapping("/api/v1/vacation-policies")
    public ApiResponse createVacationPolicy(@RequestBody VacationApiDto.CreateVacationPolicyReq data) {
        Long vacationPolicyId = vacationService.createVacationPolicy(VacationPolicyServiceDto.builder()
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
                .effectiveType(data.getEffectiveType())
                .expirationType(data.getExpirationType())
                .approvalRequiredCount(data.getApprovalRequiredCount())
                .build()
        );

        return ApiResponse.success(new VacationApiDto.CreateVacationPolicyResp(vacationPolicyId));
    }

    /**
     * 특정 휴가 정책 조회
     * GET /api/v1/vacation-policies/{id}
     */
    @GetMapping("/api/v1/vacation-policies/{id}")
    public ApiResponse getVacationPolicy(@PathVariable("id") Long vacationPolicyId) {
        VacationPolicyServiceDto policy = vacationService.getVacationPolicy(vacationPolicyId);

        return ApiResponse.success(new VacationApiDto.GetVacationPolicyResp(
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
                policy.getSpecificDays(),
                policy.getEffectiveType(),
                policy.getExpirationType(),
                policy.getRepeatGrantDescription()
        ));
    }

    /**
     * 휴가 정책 목록 조회
     * GET /api/v1/vacation-policies
     */
    @GetMapping("/api/v1/vacation-policies")
    public ApiResponse getVacationPolicies() {
        List<VacationPolicyServiceDto> policies = vacationService.getVacationPolicies();

        List<VacationApiDto.GetVacationPolicyResp> resp = policies.stream()
                .map(vp -> new VacationApiDto.GetVacationPolicyResp(
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
                        vp.getSpecificDays(),
                        vp.getEffectiveType(),
                        vp.getExpirationType(),
                        vp.getRepeatGrantDescription()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    /**
     * 휴가 정책 삭제
     * DELETE /api/v1/vacation-policies/{id}
     */
    @DeleteMapping("/api/v1/vacation-policies/{id}")
    public ApiResponse deleteVacationPolicy(@PathVariable("id") Long vacationPolicyId) {
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
    public ApiResponse getUserAssignedVacationPolicies(@PathVariable("userId") String userId) {
        List<VacationPolicyServiceDto> policies = vacationService.getUserAssignedVacationPolicies(userId);

        List<VacationApiDto.GetUserAssignedVacationPoliciesResp> resp = policies.stream()
                .map(vp -> new VacationApiDto.GetUserAssignedVacationPoliciesResp(
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
                        vp.getSpecificDays(),
                        vp.getEffectiveType(),
                        vp.getExpirationType(),
                        vp.getRepeatGrantDescription()
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

    /**
     * 관리자가 특정 사용자에게 휴가를 직접 부여
     * POST /api/v1/users/{userId}/vacation-grants
     */
    @PostMapping("/api/v1/users/{userId}/vacation-grants")
    public ApiResponse manualGrantVacation(
            @PathVariable("userId") String userId,
            @RequestBody VacationApiDto.ManualGrantVacationReq data) {

        // DTO 변환
        VacationServiceDto serviceDto = VacationServiceDto.builder()
                .policyId(data.getVacationPolicyId())
                .grantTime(data.getGrantTime())
                .grantDate(data.getGrantDate())
                .expiryDate(data.getExpiryDate())
                .desc(data.getGrantDesc())
                .build();

        VacationGrant grant = vacationService.manualGrantVacation(userId, serviceDto);

        return ApiResponse.success(new VacationApiDto.ManualGrantVacationResp(
                grant.getId(),
                userId,
                grant.getPolicy().getId(),
                grant.getGrantTime(),
                grant.getGrantDate(),
                grant.getExpiryDate()
        ));
    }

    /**
     * 특정 휴가 부여 회수 (관리자가 직접 부여한 휴가를 취소)
     * DELETE /api/v1/vacation-grants/{vacationGrantId}
     */
    @DeleteMapping("/api/v1/vacation-grants/{vacationGrantId}")
    public ApiResponse revokeVacationGrant(@PathVariable("vacationGrantId") Long vacationGrantId) {
        VacationGrant grant = vacationService.revokeVacationGrant(vacationGrantId);

        return ApiResponse.success(new VacationApiDto.RevokeVacationGrantResp(
                grant.getId(),
                grant.getUser().getId()
        ));
    }

    /**
     * 휴가 신청 (ON_REQUEST 방식)
     * POST /api/v1/users/{userId}/vacation-requests
     */
    @PostMapping("/api/v1/users/{userId}/vacation-requests")
    public ApiResponse requestVacation(
            @PathVariable("userId") String userId,
            @RequestBody VacationApiDto.RequestVacationReq data) {

        Long vacationGrantId = vacationService.requestVacation(userId, VacationServiceDto.builder()
                .policyId(data.getPolicyId())
                .desc(data.getDesc())
                .approverIds(data.getApproverIds())
                .build());

        return ApiResponse.success(new VacationApiDto.RequestVacationResp(vacationGrantId));
    }

    /**
     * 휴가 승인
     * POST /api/v1/vacation-approvals/{approvalId}/approve
     */
    @PostMapping("/api/v1/vacation-approvals/{approvalId}/approve")
    public ApiResponse approveVacation(
            @PathVariable("approvalId") Long approvalId,
            @RequestParam("approverId") String approverId) {

        Long processedApprovalId = vacationService.approveVacation(approvalId, approverId);

        return ApiResponse.success(new VacationApiDto.ApproveVacationResp(processedApprovalId));
    }

    /**
     * 휴가 거부
     * POST /api/v1/vacation-approvals/{approvalId}/reject
     */
    @PostMapping("/api/v1/vacation-approvals/{approvalId}/reject")
    public ApiResponse rejectVacation(
            @PathVariable("approvalId") Long approvalId,
            @RequestParam("approverId") String approverId,
            @RequestBody VacationApiDto.RejectVacationReq data) {

        Long processedApprovalId = vacationService.rejectVacation(
                approvalId,
                approverId,
                VacationApprovalServiceDto.builder()
                        .rejectionReason(data.getRejectionReason())
                        .build()
        );

        return ApiResponse.success(new VacationApiDto.RejectVacationResp(processedApprovalId));
    }

    /**
     * 승인자의 대기 중인 승인 목록 조회
     * GET /api/v1/users/{approverId}/pending-approvals
     */
    @GetMapping("/api/v1/users/{approverId}/pending-approvals")
    public ApiResponse getPendingApprovalsByApprover(@PathVariable("approverId") String approverId) {
        List<VacationApprovalServiceDto> approvals = vacationService.getPendingApprovalsByApprover(approverId);

        List<VacationApiDto.GetPendingApprovalsByApproverResp.PendingApprovalInfo> approvalInfos = approvals.stream()
                .map(a -> new VacationApiDto.GetPendingApprovalsByApproverResp.PendingApprovalInfo(
                        a.getId(),
                        a.getVacationGrantId(),
                        a.getRequesterId(),
                        a.getRequesterName(),
                        a.getPolicyId(),
                        a.getPolicyName(),
                        a.getDesc(),
                        a.getRequestDate(),
                        a.getGrantTime(),
                        a.getVacationType(),
                        a.getVacationType().getViewName(),
                        a.getApprovalStatus(),
                        a.getApprovalStatus().getViewName()
                ))
                .toList();

        return ApiResponse.success(new VacationApiDto.GetPendingApprovalsByApproverResp(approvalInfos));
    }
}
