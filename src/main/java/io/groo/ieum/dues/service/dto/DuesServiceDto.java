package io.groo.ieum.dues.service.dto;

import io.groo.ieum.dues.type.DuesCalcType;
import io.groo.ieum.dues.type.DuesType;
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