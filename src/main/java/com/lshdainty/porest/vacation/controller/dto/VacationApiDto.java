package com.lshdainty.porest.vacation.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.type.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class VacationApiDto {

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UseVacationReq {
        private String userId;
        private VacationType vacationType;
        private String vacationDesc;
        private VacationTimeType vacationTimeType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UseVacationResp {
        private Long vacationUsageId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetUserVacationHistoryResp {
        private List<VacationGrantInfo> grants;  // 부여받은 내역
        private List<VacationUsageInfo> usages;  // 사용한 내역

        @Getter
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class VacationGrantInfo {
            private Long vacationGrantId;
            private VacationType vacationType;
            private String vacationTypeName;
            private String vacationGrantDesc;
            private BigDecimal grantTime;
            private BigDecimal remainTime;
            private LocalDateTime grantDate;
            private LocalDateTime expiryDate;
        }

        @Getter
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class VacationUsageInfo {
            private Long vacationUsageId;
            private String vacationUsageDesc;
            private VacationTimeType vacationTimeType;
            private String vacationTimeTypeName;
            private BigDecimal usedTime;
            private LocalDateTime startDate;
            private LocalDateTime endDate;
        }
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetAllUsersVacationHistoryResp {
        private String userId;
        private String userName;
        private List<VacationGrantInfo> grants;  // 부여받은 내역
        private List<VacationUsageInfo> usages;  // 사용한 내역

        @Getter
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class VacationGrantInfo {
            private Long vacationGrantId;
            private VacationType vacationType;
            private String vacationTypeName;
            private String vacationGrantDesc;
            private BigDecimal grantTime;
            private BigDecimal remainTime;
            private LocalDateTime grantDate;
            private LocalDateTime expiryDate;
        }

        @Getter
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class VacationUsageInfo {
            private Long vacationUsageId;
            private String vacationUsageDesc;
            private VacationTimeType vacationTimeType;
            private String vacationTimeTypeName;
            private BigDecimal usedTime;
            private LocalDateTime startDate;
            private LocalDateTime endDate;
        }
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetAvailableVacationsResp {
        private VacationType vacationType;
        private String vacationTypeName;
        private BigDecimal totalRemainTime;
        private String totalRemainTimeStr;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetVacationUsagesByPeriodResp {
        private String userId;
        private String userName;
        private Long vacationUsageId;
        private String vacationUsageDesc;
        private VacationTimeType vacationTimeType;
        private String vacationTimeTypeName;
        private BigDecimal usedTime;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetUserVacationUsagesByPeriodResp {
        private Long vacationUsageId;
        private String vacationUsageDesc;
        private VacationTimeType vacationTimeType;
        private String vacationTimeTypeName;
        private BigDecimal usedTime;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetUserMonthlyVacationStatsResp {
        private Integer month;
        private BigDecimal usedTime;
        private String usedTimeStr;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetUserVacationStatsResp {
        private BigDecimal remainTime;
        private String remainTimeStr;
        private BigDecimal usedTime;
        private String usedTimeStr;
        private BigDecimal expectUsedTime;
        private String expectUsedTimeStr;
        private BigDecimal prevRemainTime;
        private String prevRemainTimeStr;
        private BigDecimal prevUsedTime;
        private String prevUsedTimeStr;
        private BigDecimal prevExpectUsedTime;
        private String prevExpectUsedTimeStr;
        private BigDecimal remainTimeGap;
        private String remainTimeGapStr;
        private BigDecimal usedTimeGap;
        private String usedTimeGapStr;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetVacationPolicyResp {
        private Long vacationPolicyId;
        private String vacationPolicyName;
        private String vacationPolicyDesc;
        private VacationType vacationType;
        private GrantMethod grantMethod;
        private BigDecimal grantTime;
        private String grantTimeStr;
        private RepeatUnit repeatUnit;
        private Integer repeatInterval;
        private Integer specificMonths;
        private Integer specificDays;
        private EffectiveType effectiveType;
        private ExpirationType expirationType;
        private String repeatGrantDesc; // 반복 부여 정책의 한국어 설명
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreateVacationPolicyReq {
        private String vacationPolicyName;
        private String vacationPolicyDesc;
        private VacationType vacationType;
        private GrantMethod grantMethod;
        private BigDecimal grantTime;
        private RepeatUnit repeatUnit;
        private Integer repeatInterval;
        private Integer specificMonths;
        private Integer specificDays;
        private LocalDateTime firstGrantDate;  // 첫 부여 시점 (반복 부여 방식에서 필수)
        private YNType isRecurring;            // 반복 여부 (Y: 반복, N: 1회)
        private Integer maxGrantCount;         // 최대 부여 횟수 (1회성 정책용)
        private EffectiveType effectiveType;   // 유효기간 발효일 타입
        private ExpirationType expirationType; // 유효기간 만료일 타입
        private Integer approvalRequiredCount;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CreateVacationPolicyResp {
        private Long vacationPolicyId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AssignVacationPoliciesToUserReq {
        private List<Long> vacationPolicyIds;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class AssignVacationPoliciesToUserResp {
        private String userId;
        private List<Long> assignedVacationPolicyIds;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetUserAssignedVacationPoliciesResp {
        private Long userVacationPolicyId;
        private Long vacationPolicyId;
        private String vacationPolicyName;
        private String vacationPolicyDesc;
        private VacationType vacationType;
        private GrantMethod grantMethod;
        private BigDecimal grantTime;
        private String grantTimeStr;
        private RepeatUnit repeatUnit;
        private Integer repeatInterval;
        private Integer specificMonths;
        private Integer specificDays;
        private EffectiveType effectiveType;
        private ExpirationType expirationType;
        private String repeatGrantDescription; // 반복 부여 정책의 한국어 설명
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RevokeVacationPolicyFromUserResp {
        private String userId;
        private Long vacationPolicyId;
        private Long userVacationPolicyId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RevokeVacationPoliciesFromUserReq {
        private List<Long> vacationPolicyIds;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RevokeVacationPoliciesFromUserResp {
        private String userId;
        private List<Long> revokedVacationPolicyIds;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DeleteVacationPolicyResp {
        private Long vacationPolicyId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ManualGrantVacationReq {
        private Long vacationPolicyId;
        private BigDecimal grantTime;
        private LocalDateTime grantDate;
        private LocalDateTime expiryDate;
        private String grantDesc;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ManualGrantVacationResp {
        private Long vacationGrantId;
        private String userId;
        private Long vacationPolicyId;
        private BigDecimal grantTime;
        private LocalDateTime grantDate;
        private LocalDateTime expiryDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RevokeVacationGrantResp {
        private Long vacationGrantId;
        private String userId;
    }

    // ========== 휴가 신청 및 승인 관련 DTO ==========

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RequestVacationReq {
        private Long policyId;
        private String desc;
        private List<String> approverIds;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RequestVacationResp {
        private Long vacationGrantId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ApproveVacationResp {
        private Long approvalId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RejectVacationReq {
        private String rejectionReason;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RejectVacationResp {
        private Long approvalId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GetPendingApprovalsByApproverResp {
        private List<PendingApprovalInfo> pendingApprovals;

        @Getter
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class PendingApprovalInfo {
            private Long approvalId;
            private Long vacationGrantId;
            private String requesterId;
            private String requesterName;
            private Long policyId;
            private String policyName;
            private String desc;
            private LocalDateTime requestDate;
            private BigDecimal grantTime;
            private VacationType vacationType;
            private String vacationTypeName;
            private ApprovalStatus approvalStatus;
            private String approvalStatusName;
        }
    }
}
