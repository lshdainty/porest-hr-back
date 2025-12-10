package com.lshdainty.porest.vacation.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.vacation.controller.dto.VacationApiDto;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.service.VacationService;
import com.lshdainty.porest.vacation.service.VacationTimeFormatter;
import com.lshdainty.porest.vacation.service.dto.VacationApprovalServiceDto;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.service.dto.VacationServiceDto;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.GrantStatus;
import com.lshdainty.porest.vacation.type.VacationType;
import com.lshdainty.porest.common.type.DisplayType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class VacationApiController implements VacationApi {
    private final VacationService vacationService;
    private final MessageSource messageSource;
    private final VacationTimeFormatter vacationTimeFormatter;

    @Override
    @PreAuthorize("hasAuthority('VACATION_REQUEST')")
    public ApiResponse useVacation(VacationApiDto.UseVacationReq data) {
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

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getUserVacationHistory(String userId, Integer year) {
        VacationServiceDto vacationInfo = vacationService.getUserVacationHistory(userId, year);

        // VacationGrant 정보 변환
        List<VacationApiDto.GetUserVacationHistoryResp.VacationGrantInfo> grantInfos = vacationInfo.getGrants().stream()
                .map(g -> new VacationApiDto.GetUserVacationHistoryResp.VacationGrantInfo(
                        g.getId(),
                        g.getType(),
                        getTranslatedName(g.getType()),
                        g.getDesc(),
                        g.getGrantTime(),
                        vacationTimeFormatter.format(g.getGrantTime()),
                        g.getRemainTime(),
                        vacationTimeFormatter.format(g.getRemainTime()),
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
                        getTranslatedName(u.getType()),
                        u.getUsedTime(),
                        vacationTimeFormatter.format(u.getUsedTime()),
                        u.getStartDate(),
                        u.getEndDate()
                ))
                .toList();

        return ApiResponse.success(new VacationApiDto.GetUserVacationHistoryResp(grantInfos, usageInfos));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_MANAGE')")
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
                                            getTranslatedName(g.getType()),
                                            g.getDesc(),
                                            g.getGrantTime(),
                                            vacationTimeFormatter.format(g.getGrantTime()),
                                            g.getRemainTime(),
                                            vacationTimeFormatter.format(g.getRemainTime()),
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
                                            getTranslatedName(u.getType()),
                                            u.getUsedTime(),
                                            vacationTimeFormatter.format(u.getUsedTime()),
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

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getAvailableVacations(String userId, LocalDateTime startDate) {
        List<VacationServiceDto> availableVacations = vacationService.getAvailableVacations(userId, startDate);

        // 타입별 잔여 휴가 리스트 생성
        List<VacationApiDto.AvailableVacationByType> vacationsByType = availableVacations.stream()
                .map(dto -> new VacationApiDto.AvailableVacationByType(
                        dto.getType(),
                        getTranslatedName(dto.getType()),
                        dto.getRemainTime(),
                        vacationTimeFormatter.format(dto.getRemainTime())
                ))
                .toList();

        // 전체 잔여 휴가 시간 계산
        BigDecimal totalRemainTime = availableVacations.stream()
                .map(VacationServiceDto::getRemainTime)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        VacationApiDto.GetAvailableVacationsResp resp = new VacationApiDto.GetAvailableVacationsResp(
                totalRemainTime,
                vacationTimeFormatter.format(totalRemainTime),
                vacationsByType
        );

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_REQUEST')")
    public ApiResponse updateVacationUsage(Long vacationUsageId, VacationApiDto.UpdateVacationUsageReq data) {
        Long newVacationUsageId = vacationService.updateVacationUsage(
                vacationUsageId,
                VacationServiceDto.builder()
                        .userId(data.getUserId())
                        .type(data.getVacationType())
                        .desc(data.getVacationDesc())
                        .timeType(data.getVacationTimeType())
                        .startDate(data.getStartDate())
                        .endDate(data.getEndDate())
                        .build()
        );

        return ApiResponse.success(new VacationApiDto.UpdateVacationUsageResp(newVacationUsageId));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_CANCEL')")
    public ApiResponse cancelVacationUsage(Long vacationUsageId) {
        vacationService.cancelVacationUsage(vacationUsageId);
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_MANAGE')")
    public ApiResponse getVacationUsagesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<VacationServiceDto> histories = vacationService.getVacationUsagesByPeriod(startDate, endDate);

        List<VacationApiDto.GetVacationUsagesByPeriodResp> resp = histories.stream()
                .map(dto -> new VacationApiDto.GetVacationUsagesByPeriodResp(
                        dto.getUser().getId(),
                        dto.getUser().getName(),
                        dto.getId(),
                        dto.getDesc(),
                        dto.getTimeType(),
                        getTranslatedName(dto.getTimeType()),
                        dto.getUsedTime(),
                        dto.getStartDate(),
                        dto.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getUserVacationUsagesByPeriod(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<VacationServiceDto> histories = vacationService.getUserVacationUsagesByPeriod(userId, startDate, endDate);

        List<VacationApiDto.GetUserVacationUsagesByPeriodResp> resp = histories.stream()
                .map(dto -> new VacationApiDto.GetUserVacationUsagesByPeriodResp(
                        dto.getId(),
                        dto.getDesc(),
                        dto.getTimeType(),
                        getTranslatedName(dto.getTimeType()),
                        dto.getUsedTime(),
                        dto.getStartDate(),
                        dto.getEndDate()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getUserMonthlyVacationStats(String userId, String year) {
        List<VacationServiceDto> histories = vacationService.getUserMonthlyVacationStats(userId, year);

        List<VacationApiDto.GetUserMonthlyVacationStatsResp> resp = histories.stream()
                .map(v -> new VacationApiDto.GetUserMonthlyVacationStatsResp(
                        v.getMonth(),
                        v.getUsedTime(),
                        vacationTimeFormatter.format(v.getUsedTime())
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getUserVacationStats(String userId, LocalDateTime baseDate) {
        VacationServiceDto stats = vacationService.getUserVacationStats(userId, baseDate);

        return ApiResponse.success(new VacationApiDto.GetUserVacationStatsResp(
                stats.getRemainTime(),
                vacationTimeFormatter.format(stats.getRemainTime()),
                stats.getUsedTime(),
                vacationTimeFormatter.format(stats.getUsedTime()),
                stats.getExpectUsedTime(),
                vacationTimeFormatter.format(stats.getExpectUsedTime()),
                stats.getPrevRemainTime(),
                vacationTimeFormatter.format(stats.getPrevRemainTime()),
                stats.getPrevUsedTime(),
                vacationTimeFormatter.format(stats.getPrevUsedTime()),
                stats.getPrevExpectUsedTime(),
                vacationTimeFormatter.format(stats.getPrevExpectUsedTime()),
                stats.getRemainTimeGap(),
                vacationTimeFormatter.format(stats.getRemainTimeGap().abs()),
                stats.getUsedTimeGap(),
                vacationTimeFormatter.format(stats.getUsedTimeGap().abs())
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_POLICY_MANAGE')")
    public ApiResponse createVacationPolicy(VacationApiDto.CreateVacationPolicyReq data) {
        Long vacationPolicyId = vacationService.createVacationPolicy(VacationPolicyServiceDto.builder()
                .name(data.getVacationPolicyName())
                .desc(data.getVacationPolicyDesc())
                .vacationType(data.getVacationType())
                .grantMethod(data.getGrantMethod())
                .grantTime(data.getGrantTime())
                .isFlexibleGrant(data.getIsFlexibleGrant())
                .minuteGrantYn(data.getMinuteGrantYn())
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

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getVacationPolicy(Long vacationPolicyId) {
        VacationPolicyServiceDto policy = vacationService.getVacationPolicy(vacationPolicyId);

        return ApiResponse.success(new VacationApiDto.GetVacationPolicyResp(
                policy.getId(),
                policy.getName(),
                policy.getDesc(),
                policy.getVacationType(),
                policy.getGrantMethod(),
                policy.getGrantTime(),
                vacationTimeFormatter.format(policy.getGrantTime()),
                policy.getIsFlexibleGrant(),
                policy.getMinuteGrantYn(),
                policy.getRepeatUnit(),
                policy.getRepeatInterval(),
                policy.getSpecificMonths(),
                policy.getSpecificDays(),
                policy.getEffectiveType(),
                policy.getExpirationType(),
                policy.getRepeatGrantDescription()
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
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
                        vacationTimeFormatter.format(vp.getGrantTime()),
                        vp.getIsFlexibleGrant(),
                        vp.getMinuteGrantYn(),
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

    @Override
    @PreAuthorize("hasAuthority('VACATION_POLICY_MANAGE')")
    public ApiResponse deleteVacationPolicy(Long vacationPolicyId) {
        Long deletedPolicyId = vacationService.deleteVacationPolicy(vacationPolicyId);

        return ApiResponse.success(new VacationApiDto.DeleteVacationPolicyResp(deletedPolicyId));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_POLICY_MANAGE')")
    public ApiResponse assignVacationPoliciesToUser(String userId, VacationApiDto.AssignVacationPoliciesToUserReq data) {
        List<Long> assignedPolicyIds = vacationService.assignVacationPoliciesToUser(userId, data.getVacationPolicyIds());

        return ApiResponse.success(new VacationApiDto.AssignVacationPoliciesToUserResp(
                userId,
                assignedPolicyIds
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getUserAssignedVacationPolicies(String userId, GrantMethod grantMethod) {
        List<VacationPolicyServiceDto> policies = vacationService.getUserAssignedVacationPolicies(userId, grantMethod);

        List<VacationApiDto.GetUserAssignedVacationPoliciesResp> resp = policies.stream()
                .map(vp -> new VacationApiDto.GetUserAssignedVacationPoliciesResp(
                        vp.getUserVacationPolicyId(),
                        vp.getId(),
                        vp.getName(),
                        vp.getDesc(),
                        vp.getVacationType(),
                        vp.getGrantMethod(),
                        vp.getGrantTime(),
                        vacationTimeFormatter.format(vp.getGrantTime()),
                        vp.getIsFlexibleGrant(),
                        vp.getMinuteGrantYn(),
                        vp.getRepeatUnit(),
                        vp.getRepeatInterval(),
                        vp.getSpecificMonths(),
                        vp.getSpecificDays(),
                        vp.getFirstGrantDate(),
                        vp.getIsRecurring(),
                        vp.getMaxGrantCount(),
                        vp.getApprovalRequiredCount(),
                        vp.getEffectiveType(),
                        vp.getExpirationType(),
                        vp.getRepeatGrantDescription()
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getUserAssignedVacationPoliciesWithFilters(String userId, VacationType vacationType, GrantMethod grantMethod) {
        List<VacationPolicyServiceDto> policies = vacationService.getUserAssignedVacationPoliciesWithFilters(
                userId, vacationType, grantMethod);

        List<VacationApiDto.GetVacationPolicyAssignmentStatusResp.VacationPolicyInfo> resp = policies.stream()
                .map(vp -> new VacationApiDto.GetVacationPolicyAssignmentStatusResp.VacationPolicyInfo(
                        vp.getId(),
                        vp.getName(),
                        vp.getDesc(),
                        vp.getVacationType(),
                        vp.getGrantMethod(),
                        vp.getGrantTime(),
                        vacationTimeFormatter.format(vp.getGrantTime()),
                        vp.getIsFlexibleGrant(),
                        vp.getMinuteGrantYn(),
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

    @Override
    @PreAuthorize("hasAuthority('VACATION_POLICY_MANAGE')")
    public ApiResponse revokeVacationPolicyFromUser(String userId, Long vacationPolicyId) {
        Long userVacationPolicyId = vacationService.revokeVacationPolicyFromUser(userId, vacationPolicyId);

        return ApiResponse.success(new VacationApiDto.RevokeVacationPolicyFromUserResp(
                userId,
                vacationPolicyId,
                userVacationPolicyId
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_POLICY_MANAGE')")
    public ApiResponse revokeVacationPoliciesFromUser(String userId, VacationApiDto.RevokeVacationPoliciesFromUserReq data) {
        List<Long> revokedPolicyIds = vacationService.revokeVacationPoliciesFromUser(userId, data.getVacationPolicyIds());

        return ApiResponse.success(new VacationApiDto.RevokeVacationPoliciesFromUserResp(
                userId,
                revokedPolicyIds
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_GRANT')")
    public ApiResponse manualGrantVacation(String userId, VacationApiDto.ManualGrantVacationReq data) {

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

    @Override
    @PreAuthorize("hasAuthority('VACATION_GRANT')")
    public ApiResponse revokeVacationGrant(Long vacationGrantId) {
        VacationGrant grant = vacationService.revokeVacationGrant(vacationGrantId);

        return ApiResponse.success(new VacationApiDto.RevokeVacationGrantResp(
                grant.getId(),
                grant.getUser().getId()
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_REQUEST')")
    public ApiResponse requestVacation(String userId, VacationApiDto.RequestVacationReq data) {

        Long vacationGrantId = vacationService.requestVacation(userId, VacationServiceDto.builder()
                .policyId(data.getPolicyId())
                .desc(data.getDesc())
                .approverIds(data.getApproverIds())
                .grantTime(data.getGrantTime())
                .requestStartTime(data.getRequestStartTime())
                .requestEndTime(data.getRequestEndTime())
                .requestDesc(data.getRequestDesc())
                .build());

        return ApiResponse.success(new VacationApiDto.RequestVacationResp(vacationGrantId));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_APPROVE')")
    public ApiResponse approveVacation(Long approvalId, String approverId) {

        Long processedApprovalId = vacationService.approveVacation(approvalId, approverId);

        return ApiResponse.success(new VacationApiDto.ApproveVacationResp(processedApprovalId));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_APPROVE')")
    public ApiResponse rejectVacation(Long approvalId, String approverId, VacationApiDto.RejectVacationReq data) {

        Long processedApprovalId = vacationService.rejectVacation(
                approvalId,
                approverId,
                VacationApprovalServiceDto.builder()
                        .rejectionReason(data.getRejectionReason())
                        .build()
        );

        return ApiResponse.success(new VacationApiDto.RejectVacationResp(processedApprovalId));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_CANCEL')")
    public ApiResponse cancelVacationRequest(Long vacationGrantId, String userId) {

        Long canceledVacationGrantId = vacationService.cancelVacationRequest(vacationGrantId, userId);

        return ApiResponse.success(new VacationApiDto.CancelVacationRequestResp(canceledVacationGrantId));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_APPROVE')")
    public ApiResponse getAllVacationsByApprover(String approverId, Integer year, GrantStatus status) {
        List<VacationServiceDto> vacations = vacationService.getAllVacationsByApprover(approverId, year, status);

        List<VacationApiDto.GetUserRequestedVacationsResp> resp = vacations.stream()
                .map(v -> {
                    // 승인자 목록을 ApproverInfo DTO로 변환
                    List<VacationApiDto.GetUserRequestedVacationsResp.ApproverInfo> approvers = null;
                    if (v.getApprovers() != null) {
                        approvers = v.getApprovers().stream()
                                .map(approver -> new VacationApiDto.GetUserRequestedVacationsResp.ApproverInfo(
                                        approver.getId(),
                                        approver.getApproverId(),
                                        approver.getApproverName(),
                                        approver.getApprovalOrder(),
                                        approver.getApprovalStatus(),
                                        getTranslatedName(approver.getApprovalStatus()),
                                        approver.getApprovalDate(),
                                        approver.getRejectionReason()
                                ))
                                .toList();
                    }

                    return new VacationApiDto.GetUserRequestedVacationsResp(
                            v.getId(),
                            v.getPolicyId(),
                            v.getPolicyName(),
                            v.getType(),
                            getTranslatedName(v.getType()),
                            v.getDesc(),
                            v.getGrantTime(),
                            vacationTimeFormatter.format(v.getGrantTime()),
                            v.getPolicyGrantTime(),
                            vacationTimeFormatter.format(v.getPolicyGrantTime()),
                            v.getRemainTime(),
                            vacationTimeFormatter.format(v.getRemainTime()),
                            v.getGrantDate(),
                            v.getExpiryDate(),
                            v.getRequestStartTime(),
                            v.getRequestEndTime(),
                            v.getRequestDesc(),
                            v.getGrantStatus(),
                            getTranslatedName(v.getGrantStatus()),
                            v.getCreateDate(),
                            v.getCurrentApproverId(),
                            v.getCurrentApproverName(),
                            approvers
                    );
                })
                .toList();

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getUserRequestedVacations(String userId, Integer year) {
        List<VacationServiceDto> requestedVacations = vacationService.getAllRequestedVacationsByUserId(userId, year);

        List<VacationApiDto.GetUserRequestedVacationsResp> resp = requestedVacations.stream()
                .map(v -> {
                    // 승인자 목록을 ApproverInfo DTO로 변환
                    List<VacationApiDto.GetUserRequestedVacationsResp.ApproverInfo> approvers = null;
                    if (v.getApprovers() != null) {
                        approvers = v.getApprovers().stream()
                                .map(approver -> new VacationApiDto.GetUserRequestedVacationsResp.ApproverInfo(
                                        approver.getId(),
                                        approver.getApproverId(),
                                        approver.getApproverName(),
                                        approver.getApprovalOrder(),
                                        approver.getApprovalStatus(),
                                        getTranslatedName(approver.getApprovalStatus()),
                                        approver.getApprovalDate(),
                                        approver.getRejectionReason()
                                ))
                                .toList();
                    }

                    return new VacationApiDto.GetUserRequestedVacationsResp(
                            v.getId(),
                            v.getPolicyId(),
                            v.getPolicyName(),
                            v.getType(),
                            getTranslatedName(v.getType()),
                            v.getDesc(),
                            v.getGrantTime(),
                            vacationTimeFormatter.format(v.getGrantTime()),
                            v.getPolicyGrantTime(),
                            vacationTimeFormatter.format(v.getPolicyGrantTime()),
                            v.getRemainTime(),
                            vacationTimeFormatter.format(v.getRemainTime()),
                            v.getGrantDate(),
                            v.getExpiryDate(),
                            v.getRequestStartTime(),
                            v.getRequestEndTime(),
                            v.getRequestDesc(),
                            v.getGrantStatus(),
                            getTranslatedName(v.getGrantStatus()),
                            v.getCreateDate(),
                            v.getCurrentApproverId(),
                            v.getCurrentApproverName(),
                            approvers
                    );
                })
                .toList();

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_READ')")
    public ApiResponse getUserRequestedVacationStats(String userId, Integer year) {
        VacationServiceDto stats = vacationService.getRequestedVacationStatsByUserId(userId, year);

        return ApiResponse.success(new VacationApiDto.GetUserRequestedVacationStatsResp(
                stats.getTotalRequestCount(),
                stats.getCurrentMonthRequestCount(),
                stats.getChangeRate(),
                stats.getPendingCount(),
                stats.getAverageProcessingDays(),
                stats.getProgressCount(),
                stats.getApprovedCount(),
                stats.getApprovalRate(),
                stats.getRejectedCount(),
                stats.getCanceledCount(),
                stats.getAcquiredVacationTimeStr(),
                stats.getAcquiredVacationTime()
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('VACATION_MANAGE')")
    public ApiResponse getVacationPolicyAssignmentStatus(String userId) {
        VacationServiceDto result = vacationService.getVacationPolicyAssignmentStatus(userId);

        // 할당된 정책 변환
        List<VacationApiDto.GetVacationPolicyAssignmentStatusResp.VacationPolicyInfo> assignedPolicies =
                result.getAssignedPolicies().stream()
                        .map(vp -> new VacationApiDto.GetVacationPolicyAssignmentStatusResp.VacationPolicyInfo(
                                vp.getId(),
                                vp.getName(),
                                vp.getDesc(),
                                vp.getVacationType(),
                                vp.getGrantMethod(),
                                vp.getGrantTime(),
                                vacationTimeFormatter.format(vp.getGrantTime()),
                                vp.getIsFlexibleGrant(),
                                vp.getMinuteGrantYn(),
                                vp.getRepeatUnit(),
                                vp.getRepeatInterval(),
                                vp.getSpecificMonths(),
                                vp.getSpecificDays(),
                                vp.getEffectiveType(),
                                vp.getExpirationType(),
                                vp.getRepeatGrantDescription()
                        ))
                        .toList();

        // 할당되지 않은 정책 변환
        List<VacationApiDto.GetVacationPolicyAssignmentStatusResp.VacationPolicyInfo> unassignedPolicies =
                result.getUnassignedPolicies().stream()
                        .map(vp -> new VacationApiDto.GetVacationPolicyAssignmentStatusResp.VacationPolicyInfo(
                                vp.getId(),
                                vp.getName(),
                                vp.getDesc(),
                                vp.getVacationType(),
                                vp.getGrantMethod(),
                                vp.getGrantTime(),
                                vacationTimeFormatter.format(vp.getGrantTime()),
                                vp.getIsFlexibleGrant(),
                                vp.getMinuteGrantYn(),
                                vp.getRepeatUnit(),
                                vp.getRepeatInterval(),
                                vp.getSpecificMonths(),
                                vp.getSpecificDays(),
                                vp.getEffectiveType(),
                                vp.getExpirationType(),
                                vp.getRepeatGrantDescription()
                        ))
                        .toList();

        return ApiResponse.success(new VacationApiDto.GetVacationPolicyAssignmentStatusResp(
                assignedPolicies,
                unassignedPolicies
        ));
    }

    // ========== 전체 유저 휴가 통계 조회 ==========

    @Override
    @PreAuthorize("hasAuthority('VACATION_MANAGE')")
    public ApiResponse getAllUsersVacationSummary(Integer year) {
        List<VacationServiceDto> userVacationSummaries = vacationService.getAllUsersVacationSummary(year);

        List<VacationApiDto.GetAllUsersVacationSummaryResp> resp = userVacationSummaries.stream()
                .map(dto -> new VacationApiDto.GetAllUsersVacationSummaryResp(
                        dto.getUser().getId(),
                        dto.getUser().getName(),
                        dto.getDepartmentName(),
                        dto.getTotalVacationDays(),
                        vacationTimeFormatter.format(dto.getTotalVacationDays()),
                        dto.getUsedVacationDays(),
                        vacationTimeFormatter.format(dto.getUsedVacationDays()),
                        dto.getScheduledVacationDays(),
                        vacationTimeFormatter.format(dto.getScheduledVacationDays()),
                        dto.getRemainingVacationDays(),
                        vacationTimeFormatter.format(dto.getRemainingVacationDays())
                ))
                .toList();

        return ApiResponse.success(resp);
    }

    private String getTranslatedName(DisplayType type) {
        if (type == null) return null;
        return messageSource.getMessage(type.getMessageKey(), null, LocaleContextHolder.getLocale());
    }
}
