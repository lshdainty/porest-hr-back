package com.lshdainty.myhr.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum VacationTimeType {
    DAYOFF("연차", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(1.0000))),
    MORNINGOFF("오전반차", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    AFTERNOONOFF("오후반차", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    ONETIMEOFF("1시간 휴가", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.1250))),
    TWOTIMEOFF("2시간 휴가", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.2500))),
    THREETIMEOFF("3시간 휴가", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.3750))),
    FIVETIMEOFF("5시간 휴가", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.6250))),
    SIXTIMEOFF("6시간 휴가", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.7500))),
    SEVENTIMEOFF("7시간 휴가", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.8750))),
    HALFTIMEOFF("30분 휴가", dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.0625)));

    private String strName;
    private Function<BigDecimal, BigDecimal> expression;

    VacationTimeType(String strName, Function<BigDecimal, BigDecimal> expression) {
        this.strName = strName;
        this.expression = expression;
    }

    public BigDecimal convertToValue(int dayCount) {
        return expression.apply(BigDecimal.valueOf(dayCount));
    }
}
