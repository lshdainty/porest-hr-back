package com.lshdainty.porest.schedule.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.schedule.type.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

public class ScheduleApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RegistScheduleReq {
        private String userId;
        private ScheduleType scheduleType;
        private String scheduleDesc;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RegistScheduleResp {
        private Long scheduleId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchSchedulesByUserResp {
        private Long scheduleId;
        private ScheduleType scheduleType;
        private String scheduleTypeName;
        private String scheduleDesc;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchSchedulesByPeriodResp {
        private Long scheduleId;
        private String userId;
        private String userName;
        private ScheduleType scheduleType;
        private String scheduleTypeName;
        private String scheduleDesc;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}