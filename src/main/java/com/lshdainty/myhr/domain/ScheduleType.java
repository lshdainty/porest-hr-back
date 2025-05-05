package com.lshdainty.myhr.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum ScheduleType {
    DAYOFF("연차", "1", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(1.0000))),           // 연차
    MORNINGOFF("오전반차", "2", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),       // 오전반차
    AFTERNOONOFF("오후반차", "3", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.5000))),     // 오후반차
    ONETIMEOFF("1시간 휴가", "7", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.1250))),       // 1시간 휴가
    TWOTIMEOFF("2시간 휴가", "8", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.2500))),       // 2시간 휴가
    THREETIMEOFF("3시간 휴가", "9", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.3750))),     // 3시간 휴가
    FIVETIMEOFF("5시간 휴가", "10", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.6250))),     // 5시간 휴가
    SIXTIMEOFF("6시간 휴가", "11", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.7500))),      // 6시간 휴가
    SEVENTIMEOFF("7시간 휴가", "12", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.8750))),    // 7시간 휴가
    HALFTIMEOFF("30분 휴가", "13", true, dayDiff -> dayDiff.multiply(BigDecimal.valueOf(0.0625))),   // 30분 휴가
    EDUCATION("교육", "6", false),                                                           // 교육
    BIRTHDAY("생일", "5", false),                                                            // 생일
    BUSINESSTRIP("출장", "4", false),                                                        // 출장
    DEFENSE("민방위", "15", false),                                                            // 민방위
    DEFENSEHALF("민방위(반차)", "14", false),                                                        // 민방위(반차)
    HEALTHCHECK("건강검진", "17", false),                                                        // 건강검진
    HEALTHCHECKHALF("건강검진(반차)", "16", false),                                                    // 건강검진(반차)
    BIRTHPARTY("생일파티", "18", false);                                                         // 생일파티

    private String typeName;
    private String oldType;
    private Boolean vacationType;
    private Function<BigDecimal, BigDecimal> expression;

    ScheduleType(String typeName, String oldType, Boolean vacationType, Function<BigDecimal, BigDecimal> expression) {
        this.typeName = typeName;
        this.oldType = oldType;
        this.vacationType = vacationType;
        this.expression = expression;
    }

    ScheduleType(String typeName, String oldType, Boolean vacationType) {
        this.typeName = typeName;
        this.oldType = oldType;
        this.vacationType = vacationType;
    }

    public Boolean isVacationType() {
        return vacationType;
    }

    public BigDecimal convertToValue(int dayCount) {
        return expression.apply(BigDecimal.valueOf(dayCount));
    }
}
