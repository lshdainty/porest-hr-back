package com.lshdainty.porest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.porest.type.DuesCalcType;
import com.lshdainty.porest.type.DuesType;
import lombok.*;

import java.util.List;

@Getter @Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DuesDto {
    private Long duesSeq;
    private String duesUserName;
    private Long duesAmount;
    private DuesType duesType;
    private DuesCalcType duesCalc;
    private String duesDate;
    private String duesDetail;

    private Long totalDues;
    private Long totalDeposit;
    private Long totalWithdrawal;

    private Long birthMonthDues;

    private List<Long> monthBirthDues;
}
