package com.lshdainty.porest.dues.controller.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class DuesApiDto {
    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RegistDuesReq {
        private String duesUserName;
        private Long duesAmount;
        private DuesType duesType;
        private DuesCalcType duesCalc;
        private String duesDate;
        private String duesDetail;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class RegistDuesResp {
        private Long duesSeq;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EditDuesReq {
        private String duesUserName;
        private Long duesAmount;
        private DuesType duesType;
        private DuesCalcType duesCalc;
        private String duesDate;
        private String duesDetail;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchYearDuesResp {
        private Long duesSeq;
        private String duesUserName;
        private Long duesAmount;
        private DuesType duesType;
        private DuesCalcType duesCalc;
        private String duesDate;
        private String duesDetail;
        private Long totalDues;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchYearOperationDuesResp {
        private Long totalDues;
        private Long totalDeposit;
        private Long totalWithdrawal;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchMonthBirthDuesResp {
        private Long birthMonthDues;
    }

    @Getter
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class SearchUsersMonthBirthDuesResp {
        private String duesUserName;
        private List<Long> monthBirthDues;
    }
}