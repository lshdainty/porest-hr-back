package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

import java.time.LocalDateTime;

public enum EffectiveType implements DisplayType {
    IMMEDIATELY("부여 즉시", 1L),
    START_OF_YEAR("당해년도 1월 1일", 2L);

    private String strName;
    private Long orderSeq;

    EffectiveType(String strName, Long orderSeq) {
        this.strName = strName;
        this.orderSeq = orderSeq;
    }

    @Override
    public String getViewName() {
        return strName;
    }

    @Override
    public Long getOrderSeq() {
        return orderSeq;
    }

    /**
     * effectiveType에 따라 휴가 발효일 계산
     *
     * @param baseTime 기준 시간
     * @return 계산된 발효일
     */
    public LocalDateTime calculateDate(LocalDateTime baseTime) {
        switch (this) {
            case IMMEDIATELY:
                // 부여 즉시
                return baseTime;
            case START_OF_YEAR:
                // 당해년도 1월 1일 00:00:00
                return LocalDateTime.of(baseTime.getYear(), 1, 1, 0, 0, 0);
            default:
                throw new IllegalArgumentException("지원하지 않는 EffectiveType입니다: " + this);
        }
    }
}
