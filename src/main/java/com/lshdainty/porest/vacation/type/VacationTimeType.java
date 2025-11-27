package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum VacationTimeType implements DisplayType {
    DAYOFF("연차", 1L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(1.0000))),
    MORNINGOFF("오전반차", 2L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    AFTERNOONOFF("오후반차", 3L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    ONETIMEOFF("1시간 휴가", 4L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.1250))),
    TWOTIMEOFF("2시간 휴가", 5L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.2500))),
    THREETIMEOFF("3시간 휴가", 6L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.3750))),
    FIVETIMEOFF("5시간 휴가", 7L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.6250))),
    SIXTIMEOFF("6시간 휴가", 8L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.7500))),
    SEVENTIMEOFF("7시간 휴가", 9L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.8750))),
    HALFTIMEOFF("30분 휴가", 10L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.0625))),
    HEALTHCHECKHALF("건강검진(반차)", 11L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    DEFENSE("민방위", 12L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(1.0000))),
    DEFENSEHALF("민방위(반차)", 13L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000)));

    private String strName;
    private Long orderSeq;
    private Function<BigDecimal, BigDecimal> expression;

    VacationTimeType(String strName, Long orderSeq, Function<BigDecimal, BigDecimal> expression) {
        this.strName = strName;
        this.orderSeq = orderSeq;
        this.expression = expression;
    }

    @Override
    public String getViewName() {
        return strName;
    }

    @Override
    public Long getOrderSeq() {
        return orderSeq;
    }

    public BigDecimal convertToValue(int dayCount) {
        return expression.apply(BigDecimal.valueOf(dayCount));
    }

    /**
     * value -> 문자열 일수 변환 함수<br>
     * ex. 2.3750 -> 2일 3시간
     *
     * @return dayStr
     */
    public static String convertValueToDay(BigDecimal value) {
        String dayStr = "0일";

        if (Objects.isNull(value)) {
            return dayStr;
        }

        if (value.compareTo(BigDecimal.ZERO) > 0) {
            StringBuilder builer = new StringBuilder();

            // 넘어온 value에서 일 분리
            int days = value.intValue();

            // 넘어온 value에서 시간 분리
            int hours = value.remainder(BigDecimal.valueOf(1.0000)) // 0.1250 ~ 0.8750 분리
                    .divide(BigDecimal.valueOf(0.1250), 0, RoundingMode.DOWN).intValue(); // 0 ~ 7로 변환

            // 넘어온 value에서 분 분리
            int minutes = value.remainder(BigDecimal.valueOf(1.0000)) // 0.1250 ~ 0.8750 분리
                    .remainder(BigDecimal.valueOf(0.1250)) // 0.0625 분리
                    .divide(BigDecimal.valueOf(0.0625), 0, RoundingMode.DOWN).intValue() * 30; // 0 or 30

            if (days > 0) {
                builer.append(days).append("일");
            }
            if (hours > 0) {
                if (!builer.isEmpty()) builer.append(" ");
                builer.append(hours).append("시간");
            }
            if (minutes > 0) {
                if (!builer.isEmpty()) builer.append(" ");
                builer.append(minutes).append("분");
            }

            dayStr = builer.toString();
        }

        return dayStr;
    }
}
