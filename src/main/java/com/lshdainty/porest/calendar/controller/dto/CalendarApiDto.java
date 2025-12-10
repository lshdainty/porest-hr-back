package com.lshdainty.porest.calendar.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.vacation.type.VacationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

public class CalendarApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "기간별 이벤트 조회 응답")
    public static class searchEventsByPeriodResp {
        @Schema(description = "사용자 ID", example = "user123")
        private String userId;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String userName;

        @Schema(description = "캘린더 이벤트 이름 (휴가/일정 타입 이름)", example = "연차")
        private String calendarName;

        @Schema(description = "캘린더 이벤트 타입 (휴가/일정 타입)", example = "ANNUAL")
        private String calendarType;

        @Schema(description = "캘린더 이벤트 설명", example = "개인 사유")
        private String calendarDesc;

        @Schema(description = "시작 날짜", example = "2024-01-15T09:00:00")
        private LocalDateTime startDate;

        @Schema(description = "종료 날짜", example = "2024-01-15T18:00:00")
        private LocalDateTime endDate;

        @Schema(description = "도메인 타입 (vacation 또는 schedule)", example = "vacation")
        private String domainType;

        @Schema(description = "휴가 타입 (도메인 타입이 vacation일 경우)", example = "ANNUAL")
        private VacationType vacationType;

        @Schema(description = "캘린더 이벤트 ID", example = "1")
        private Long calendarId;
    }
}
