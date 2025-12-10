package com.lshdainty.porest.schedule.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.schedule.type.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

public class ScheduleApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "일정 등록 요청")
    public static class RegistScheduleReq {
        @Schema(description = "사용자 ID", example = "admin", required = true)
        private String userId;

        @Schema(description = "일정 유형", example = "MEETING", required = true)
        private ScheduleType scheduleType;

        @Schema(description = "일정 설명", example = "주간 회의")
        private String scheduleDesc;

        @Schema(description = "시작 일시", example = "2024-01-15T09:00:00", required = true)
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2024-01-15T10:00:00", required = true)
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "일정 등록 응답")
    public static class RegistScheduleResp {
        @Schema(description = "생성된 일정 ID", example = "1")
        private Long scheduleId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "일정 수정 요청")
    public static class UpdateScheduleReq {
        @Schema(description = "사용자 ID", example = "admin")
        private String userId;

        @Schema(description = "일정 유형", example = "MEETING")
        private ScheduleType scheduleType;

        @Schema(description = "일정 설명", example = "주간 회의 변경")
        private String scheduleDesc;

        @Schema(description = "시작 일시", example = "2024-01-15T09:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2024-01-15T10:00:00")
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "일정 수정 응답")
    public static class UpdateScheduleResp {
        @Schema(description = "수정된 일정 ID", example = "1")
        private Long scheduleId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "사용자별 일정 조회 응답")
    public static class SearchSchedulesByUserResp {
        @Schema(description = "일정 ID", example = "1")
        private Long scheduleId;

        @Schema(description = "일정 유형", example = "MEETING")
        private ScheduleType scheduleType;

        @Schema(description = "일정 유형 이름", example = "회의")
        private String scheduleTypeName;

        @Schema(description = "일정 설명", example = "주간 회의")
        private String scheduleDesc;

        @Schema(description = "시작 일시", example = "2024-01-15T09:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2024-01-15T10:00:00")
        private LocalDateTime endDate;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "기간별 일정 조회 응답")
    public static class SearchSchedulesByPeriodResp {
        @Schema(description = "일정 ID", example = "1")
        private Long scheduleId;

        @Schema(description = "사용자 ID", example = "admin")
        private String userId;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String userName;

        @Schema(description = "일정 유형", example = "MEETING")
        private ScheduleType scheduleType;

        @Schema(description = "일정 유형 이름", example = "회의")
        private String scheduleTypeName;

        @Schema(description = "일정 설명", example = "주간 회의")
        private String scheduleDesc;

        @Schema(description = "시작 일시", example = "2024-01-15T09:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 일시", example = "2024-01-15T10:00:00")
        private LocalDateTime endDate;
    }
}