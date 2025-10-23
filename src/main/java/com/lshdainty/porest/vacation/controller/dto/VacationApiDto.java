package com.lshdainty.porest.vacation.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
    public static class RegistVacationReq {
        private String userId;
        private String vacationDesc;
        private VacationType vacationType;
        private BigDecimal grantTime;
        private LocalDateTime occurDate;
        private LocalDateTime expiryDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RegistVacationResp {
        private Long vacationId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UseVacationReq {
        private String userId;
        private String vacationDesc;
        private VacationTimeType vacationTimeType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class UseVacationResp {
        private Long vacationId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchUserVacationsResp {
        private Long vacationId;
        private VacationType vacationType;
        private String vacationTypeName;
        private BigDecimal remainTime;
        private LocalDateTime occurDate;
        private LocalDateTime expiryDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchUserGroupVacationsResp {
        private String userId;
        private String userName;
        private List<VacationInfo> vacations;

        @Getter
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class VacationInfo {
            private Long vacationId;
            private VacationType vacationType;
            private String vacationTypeName;
            private BigDecimal remainTime;
            private LocalDateTime occurDate;
            private LocalDateTime expiryDate;
        }
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchAvailableVacationsResp {
        private Long vacationId;
        private VacationType vacationType;
        private String vacationTypeName;
        private BigDecimal remainTime;
        private LocalDateTime occurDate;
        private LocalDateTime expiryDate;
        private String remainTimeStr;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchPeriodVacationUseHistoriesResp {
        private String userId;
        private String userName;
        private Long vacationId;
        private String vacationDesc;
        private List<Long> vacationHistoryIds;
        private VacationTimeType vacationTimeType;
        private String vacationTimeTypeName;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchUserPeriodVacationUseHistoriesResp {
        private Long vacationId;
        private String vacationDesc;
        private Long vacationHistoryId;
        private VacationTimeType vacationTimeType;
        private String vacationTimeTypeName;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchUserMonthStatsVacationUseHistoriesResp {
        private Integer month;
        private BigDecimal usedTime;
        private String usedTimeStr;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchUserVacationUseStatsResp {
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
    public static class SearchVacationPoliciesResp {
        private Long vacationPolicyId;
        private String vacationPolicyName;
        private String vacationPolicyDesc;
        private VacationType vacationType;
        private GrantMethod grantMethod;
        private BigDecimal grantTime;
        private RepeatUnit repeatUnit;
        private Integer repeatInterval;
        private GrantTiming grantTiming;
        private Integer specificMonths;
        private Integer specificDays;
    }
}
