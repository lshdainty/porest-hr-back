package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

import java.time.LocalDateTime;

public enum ExpirationType implements DisplayType {
    ONE_MONTHS_AFTER_GRANT("부여일 + 1개월", 1L),
    TWO_MONTHS_AFTER_GRANT("부여일 + 2개월", 2L),
    THREE_MONTHS_AFTER_GRANT("부여일 + 3개월", 3L),
    FOUR_MONTHS_AFTER_GRANT("부여일 + 4개월", 4L),
    FIVE_MONTHS_AFTER_GRANT("부여일 + 5개월", 5L),
    SIX_MONTHS_AFTER_GRANT("부여일 + 6개월", 6L),
    END_OF_YEAR("당해년도 12월 31일", 7L);

    private String strName;
    private Long orderSeq;

    ExpirationType(String strName, Long orderSeq) {
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
     * expirationType에 따라 휴가 만료일 계산
     *
     * @param grantDate 부여일
     * @return 계산된 만료일
     */
    public LocalDateTime calculateDate(LocalDateTime grantDate) {
        switch (this) {
            case ONE_MONTHS_AFTER_GRANT:
                return grantDate.plusMonths(1).withHour(23).withMinute(59).withSecond(59);
            case TWO_MONTHS_AFTER_GRANT:
                return grantDate.plusMonths(2).withHour(23).withMinute(59).withSecond(59);
            case THREE_MONTHS_AFTER_GRANT:
                return grantDate.plusMonths(3).withHour(23).withMinute(59).withSecond(59);
            case FOUR_MONTHS_AFTER_GRANT:
                return grantDate.plusMonths(4).withHour(23).withMinute(59).withSecond(59);
            case FIVE_MONTHS_AFTER_GRANT:
                return grantDate.plusMonths(5).withHour(23).withMinute(59).withSecond(59);
            case SIX_MONTHS_AFTER_GRANT:
                return grantDate.plusMonths(6).withHour(23).withMinute(59).withSecond(59);
            case END_OF_YEAR:
                // 당해년도 12월 31일 23:59:59
                return LocalDateTime.of(grantDate.getYear(), 12, 31, 23, 59, 59);
            default:
                throw new IllegalArgumentException("지원하지 않는 ExpirationType입니다: " + this);
        }
    }
}
