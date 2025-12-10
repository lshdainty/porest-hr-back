package com.lshdainty.porest.dues.controller.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class DuesApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "회비 등록 요청")
    public static class RegistDuesReq {
        @Schema(description = "사용자 이름", example = "홍길동")
        private String duesUserName;

        @Schema(description = "회비 금액", example = "50000")
        private Long duesAmount;

        @Schema(description = "회비 유형", example = "OPERATION")
        private DuesType duesType;

        @Schema(description = "계산 타입 (PLUS: 입금, MINUS: 출금)", example = "PLUS")
        private DuesCalcType duesCalc;

        @Schema(description = "회비 날짜 (YYYY-MM-DD)", example = "2024-01-15")
        private LocalDate duesDate;

        @Schema(description = "상세 내용", example = "1월 회비")
        private String duesDetail;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "회비 등록 응답")
    public static class RegistDuesResp {
        @Schema(description = "등록된 회비 ID", example = "1")
        private Long duesId;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "회비 수정 요청")
    public static class EditDuesReq {
        @Schema(description = "사용자 이름", example = "홍길동")
        private String duesUserName;

        @Schema(description = "회비 금액", example = "50000")
        private Long duesAmount;

        @Schema(description = "회비 유형", example = "OPERATION")
        private DuesType duesType;

        @Schema(description = "계산 타입 (PLUS: 입금, MINUS: 출금)", example = "PLUS")
        private DuesCalcType duesCalc;

        @Schema(description = "회비 날짜 (YYYY-MM-DD)", example = "2024-01-15")
        private LocalDate duesDate;

        @Schema(description = "상세 내용", example = "1월 회비")
        private String duesDetail;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "연도별 회비 조회 응답")
    public static class SearchYearDuesResp {
        @Schema(description = "회비 ID", example = "1")
        private Long duesId;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String duesUserName;

        @Schema(description = "회비 금액", example = "50000")
        private Long duesAmount;

        @Schema(description = "회비 유형", example = "OPERATION")
        private DuesType duesType;

        @Schema(description = "계산 타입", example = "PLUS")
        private DuesCalcType duesCalc;

        @Schema(description = "회비 날짜", example = "2024-01-15")
        private LocalDate duesDate;

        @Schema(description = "상세 내용", example = "1월 회비")
        private String duesDetail;

        @Schema(description = "누적 회비 총액", example = "150000")
        private Long totalDues;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "연도별 운영 회비 조회 응답")
    public static class SearchYearOperationDuesResp {
        @Schema(description = "총 회비 (입금 - 출금)", example = "500000")
        private Long totalDues;

        @Schema(description = "총 입금액", example = "800000")
        private Long totalDeposit;

        @Schema(description = "총 출금액", example = "300000")
        private Long totalWithdrawal;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "월별 생일 회비 조회 응답")
    public static class SearchMonthBirthDuesResp {
        @Schema(description = "해당 월 생일 회비 총액", example = "100000")
        private Long birthMonthDues;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "사용자별 월별 생일 회비 조회 응답")
    public static class SearchUsersMonthBirthDuesResp {
        @Schema(description = "사용자 이름", example = "홍길동")
        private String duesUserName;

        @Schema(description = "월별 생일 회비 (1월~12월)", example = "[10000, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]")
        private List<Long> monthBirthDues;
    }
}