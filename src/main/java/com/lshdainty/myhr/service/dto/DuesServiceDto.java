package com.lshdainty.myhr.service.dto;

import com.lshdainty.myhr.domain.DuesCalcType;
import com.lshdainty.myhr.domain.DuesType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class DuesServiceDto {
    private Long seq;
    private String userName;
    private Long amount;
    private DuesType type;
    private DuesCalcType calc;
    private String date;
    private String detail;

    private Long totalDues;
    private Long totalDeposit;
    private Long totalWithdrawal;
}