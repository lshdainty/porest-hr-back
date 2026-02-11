package com.porest.hr.vacation.controller.dto;

import com.porest.core.type.YNType;
import com.porest.hr.vacation.service.dto.VacationPlanServiceDto;
import com.porest.hr.vacation.service.dto.VacationPolicyServiceDto;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
import com.porest.hr.vacation.type.GrantMethod;
import com.porest.hr.vacation.type.RepeatUnit;
import com.porest.hr.vacation.type.VacationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.List;

public class VacationPlanApiDto {

    // ========================================
    // Plan CRUD
    // ========================================

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "휴가 플랜 생성 요청")
    public static class CreatePlanReq {
        @Schema(description = "플랜 코드", example = "FULL_TIME")
        private String code;

        @Schema(description = "플랜 이름", example = "정규직 플랜")
        private String name;

        @Schema(description = "플랜 설명", example = "정규직 직원을 위한 기본 휴가 플랜")
        private String desc;

        @Schema(description = "포함할 정책 ID 목록")
        private List<Long> policyIds;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "휴가 플랜 수정 요청")
    public static class UpdatePlanReq {
        @Schema(description = "플랜 이름", example = "정규직 플랜 (수정)")
        private String name;

        @Schema(description = "플랜 설명", example = "정규직 직원을 위한 수정된 휴가 플랜")
        private String desc;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "휴가 플랜 응답")
    public static class PlanResp {
        @Schema(description = "플랜 ID", example = "1")
        private Long id;

        @Schema(description = "플랜 코드", example = "FULL_TIME")
        private String code;

        @Schema(description = "플랜 이름", example = "정규직 플랜")
        private String name;

        @Schema(description = "플랜 설명", example = "정규직 직원을 위한 기본 휴가 플랜")
        private String desc;

        @Schema(description = "포함된 정책 목록")
        private List<PolicyResp> policies;

        public static PlanResp from(VacationPlanServiceDto dto) {
            List<PolicyResp> policyResponses = null;
            if (dto.getPolicies() != null) {
                policyResponses = dto.getPolicies().stream()
                        .map(PolicyResp::from)
                        .toList();
            }
            return PlanResp.builder()
                    .id(dto.getId())
                    .code(dto.getCode())
                    .name(dto.getName())
                    .desc(dto.getDesc())
                    .policies(policyResponses)
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "휴가 정책 응답 (플랜 내)")
    public static class PolicyResp {
        @Schema(description = "정책 ID", example = "1")
        private Long id;

        @Schema(description = "정책 이름", example = "연차")
        private String name;

        @Schema(description = "정책 설명", example = "연차 휴가")
        private String desc;

        @Schema(description = "휴가 유형")
        private VacationType vacationType;

        @Schema(description = "부여 방식")
        private GrantMethod grantMethod;

        @Schema(description = "부여 시간")
        private BigDecimal grantTime;

        @Schema(description = "가변 부여 여부")
        private YNType isFlexibleGrant;

        @Schema(description = "분단위 부여 여부")
        private YNType minuteGrantYn;

        @Schema(description = "반복 단위")
        private RepeatUnit repeatUnit;

        @Schema(description = "반복 간격")
        private Integer repeatInterval;

        @Schema(description = "특정 월")
        private Integer specificMonths;

        @Schema(description = "특정 일")
        private Integer specificDays;

        @Schema(description = "효력 발생 유형")
        private EffectiveType effectiveType;

        @Schema(description = "만료 유형")
        private ExpirationType expirationType;

        public static PolicyResp from(VacationPolicyServiceDto dto) {
            return PolicyResp.builder()
                    .id(dto.getId())
                    .name(dto.getName())
                    .desc(dto.getDesc())
                    .vacationType(dto.getVacationType())
                    .grantMethod(dto.getGrantMethod())
                    .grantTime(dto.getGrantTime())
                    .isFlexibleGrant(dto.getIsFlexibleGrant())
                    .minuteGrantYn(dto.getMinuteGrantYn())
                    .repeatUnit(dto.getRepeatUnit())
                    .repeatInterval(dto.getRepeatInterval())
                    .specificMonths(dto.getSpecificMonths())
                    .specificDays(dto.getSpecificDays())
                    .effectiveType(dto.getEffectiveType())
                    .expirationType(dto.getExpirationType())
                    .build();
        }
    }

    // ========================================
    // Plan-Policy 관리
    // ========================================

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "플랜 정책 업데이트 요청")
    public static class UpdatePlanPoliciesReq {
        @Schema(description = "정책 ID 목록")
        private List<Long> policyIds;
    }

    // ========================================
    // User-Plan 관리
    // ========================================

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "사용자 플랜 할당 요청")
    public static class AssignPlanReq {
        @Schema(description = "플랜 코드", example = "FULL_TIME")
        private String planCode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "사용자 여러 플랜 할당 요청")
    public static class AssignPlansReq {
        @Schema(description = "플랜 코드 목록")
        private List<String> planCodes;
    }
}
