package com.lshdainty.porest.dues.service.dto;

import com.lshdainty.porest.dues.type.DuesCalcType;
import com.lshdainty.porest.dues.type.DuesType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@Builder
public class DuesServiceDto {
    private Long id;
    private String userName;
    private Long amount;
    private DuesType type;
    private DuesCalcType calc;
    private LocalDate date;
    private String detail;
    private Integer month;

    private Long totalDues;
    private Long totalDeposit;
    private Long totalWithdrawal;
}