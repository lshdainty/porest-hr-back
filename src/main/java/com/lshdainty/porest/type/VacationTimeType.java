package com.lshdainty.porest.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum VacationTimeType {
    DAYOFF("연차", 24*60*60L-1L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(1.0000))),
    MORNINGOFF("오전반차", 4*60*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    AFTERNOONOFF("오후반차", 4*60*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),
    ONETIMEOFF("1시간 휴가", 1*60*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.1250))),
    TWOTIMEOFF("2시간 휴가", 2*60*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.2500))),
    THREETIMEOFF("3시간 휴가", 3*60*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.3750))),
    FIVETIMEOFF("5시간 휴가", 5*60*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.6250))),
    SIXTIMEOFF("6시간 휴가", 6*60*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.7500))),
    SEVENTIMEOFF("7시간 휴가", 7*60*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.8750))),
    HALFTIMEOFF("30분 휴가", 30*60L, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.0625)));

    private String strName;
    private Long seconds;
    private Function<BigDecimal, BigDecimal> expression;

    VacationTimeType(String strName, Long seconds, Function<BigDecimal, BigDecimal> expression) {
        this.strName = strName;
        this.seconds = seconds;
        this.expression = expression;

    }

    public Long getSeconds() {
        return seconds;
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

        if (value.compareTo(BigDecimal.ZERO) > 0) {
            StringBuilder builer = new StringBuilder();

            // 넘어온 value에서 일 분리
            int days = value.intValue();

            // 넘어온 value에서 시간 분리
            int hours = value.remainder(BigDecimal.valueOf(1.0000)) // 0.1250 ~ 0.8750 분리
                    .divide(BigDecimal.valueOf(0.1250), 0, java.math.RoundingMode.DOWN).intValue(); // 0 ~ 7로 변환

            // 넘어온 value에서 분 분리
            int minutes = value.remainder(BigDecimal.valueOf(1.0000)) // 0.1250 ~ 0.8750 분리
                    .remainder(BigDecimal.valueOf(0.1250)) // 0.0625 분리
                    .divide(BigDecimal.valueOf(0.0625), 0, java.math.RoundingMode.DOWN).intValue() * 30; // 0 or 30

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
