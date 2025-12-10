package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.function.Function;

@Getter
public enum VacationTimeType implements DisplayType {
    DAYOFF(1L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(1.0000))),
    MORNINGOFF(2L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    AFTERNOONOFF(3L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    ONETIMEOFF(4L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.1250))),
    TWOTIMEOFF(5L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.2500))),
    THREETIMEOFF(6L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.3750))),
    FIVETIMEOFF(7L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.6250))),
    SIXTIMEOFF(8L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.7500))),
    SEVENTIMEOFF(9L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.8750))),
    HALFTIMEOFF(10L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.0625))),
    HEALTHCHECKHALF(11L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    DEFENSE(12L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(1.0000))),
    DEFENSEHALF(13L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000)));

    private static final String MESSAGE_KEY_PREFIX = "type.vacation.time.";
    private Long orderSeq;
    private Function<BigDecimal, BigDecimal> expression;

    VacationTimeType(Long orderSeq, Function<BigDecimal, BigDecimal> expression) {
        this.orderSeq = orderSeq;
        this.expression = expression;
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY_PREFIX + this.name().toLowerCase();
    }

    @Override
    public Long getOrderSeq() {
        return orderSeq;
    }

    public BigDecimal convertToValue(int dayCount) {
        return expression.apply(BigDecimal.valueOf(dayCount));
    }
}
