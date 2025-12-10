package com.lshdainty.porest.holiday.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.holiday.type.HolidayType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

public class HolidayApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "공휴일 등록 요청")
    public static class RegistHolidayReq {
        @Schema(description = "공휴일 이름", example = "설날")
        private String holidayName;

        @Schema(description = "공휴일 날짜 (YYYY-MM-DD)", example = "2024-02-10")
        private LocalDate holidayDate;

        @Schema(description = "공휴일 타입", example = "NATIONAL")
        private HolidayType holidayType;

        @Schema(description = "국가 코드", example = "KR")
        private CountryCode countryCode;

        @Schema(description = "음력 여부", example = "Y")
        private YNType lunarYn;

        @Schema(description = "음력 날짜 (YYYY-MM-DD)", example = "2024-01-01")
        private LocalDate lunarDate;

        @Schema(description = "매년 반복 여부", example = "Y")
        private YNType isRecurring;

        @Schema(description = "아이콘", example = "🎉")
        private String holidayIcon;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "공휴일 등록 응답")
    public static class RegistHolidayResp {
        @Schema(description = "등록된 공휴일 아이디", example = "1")
        private Long holidayId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "공휴일 수정 요청")
    public static class EditHolidayReq {
        @Schema(description = "공휴일 이름", example = "설날")
        private String holidayName;

        @Schema(description = "공휴일 날짜 (YYYY-MM-DD)", example = "2024-02-10")
        private LocalDate holidayDate;

        @Schema(description = "공휴일 타입", example = "NATIONAL")
        private HolidayType holidayType;

        @Schema(description = "국가 코드", example = "KR")
        private CountryCode countryCode;

        @Schema(description = "음력 여부", example = "Y")
        private YNType lunarYn;

        @Schema(description = "음력 날짜 (YYYY-MM-DD)", example = "2024-01-01")
        private LocalDate lunarDate;

        @Schema(description = "매년 반복 여부", example = "Y")
        private YNType isRecurring;

        @Schema(description = "아이콘", example = "🎉")
        private String holidayIcon;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "공휴일 조회 응답")
    public static class SearchHolidaysResp {
        @Schema(description = "공휴일 아이디", example = "1")
        private Long holidayId;

        @Schema(description = "공휴일 이름", example = "설날")
        private String holidayName;

        @Schema(description = "공휴일 날짜 (YYYY-MM-DD)", example = "2024-02-10")
        private LocalDate holidayDate;

        @Schema(description = "공휴일 타입", example = "NATIONAL")
        private HolidayType holidayType;

        @Schema(description = "국가 코드", example = "KR")
        private CountryCode countryCode;

        @Schema(description = "음력 여부", example = "Y")
        private YNType lunarYn;

        @Schema(description = "음력 날짜 (YYYY-MM-DD)", example = "2024-01-01")
        private LocalDate lunarDate;

        @Schema(description = "매년 반복 여부", example = "Y")
        private YNType isRecurring;

        @Schema(description = "아이콘", example = "🎉")
        private String holidayIcon;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "공휴일 수정 응답")
    public static class EditHolidayResp {
        @Schema(description = "공휴일 아이디", example = "1")
        private Long holidayId;

        @Schema(description = "공휴일 이름", example = "설날")
        private String holidayName;

        @Schema(description = "공휴일 날짜 (YYYY-MM-DD)", example = "2024-02-10")
        private LocalDate holidayDate;

        @Schema(description = "공휴일 타입", example = "NATIONAL")
        private HolidayType holidayType;

        @Schema(description = "국가 코드", example = "KR")
        private CountryCode countryCode;

        @Schema(description = "음력 여부", example = "Y")
        private YNType lunarYn;

        @Schema(description = "음력 날짜 (YYYY-MM-DD)", example = "2024-01-01")
        private LocalDate lunarDate;

        @Schema(description = "매년 반복 여부", example = "Y")
        private YNType isRecurring;

        @Schema(description = "아이콘", example = "🎉")
        private String holidayIcon;
    }
}