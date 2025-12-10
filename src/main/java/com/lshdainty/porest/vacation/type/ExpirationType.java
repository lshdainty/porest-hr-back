package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.DisplayType;

import java.time.LocalDateTime;

public enum ExpirationType implements DisplayType {
    ONE_MONTHS_AFTER_GRANT(1L),
    TWO_MONTHS_AFTER_GRANT(2L),
    THREE_MONTHS_AFTER_GRANT(3L),
    FOUR_MONTHS_AFTER_GRANT(4L),
    FIVE_MONTHS_AFTER_GRANT(5L),
    SIX_MONTHS_AFTER_GRANT(6L),
    END_OF_YEAR(7L);

    private static final String MESSAGE_KEY_PREFIX = "type.expiration.type.";
    private Long orderSeq;

    ExpirationType(Long orderSeq) {
        this.orderSeq = orderSeq;
    }

    @Override
    public String getMessageKey() {
        return MESSAGE_KEY_PREFIX + this.name().toLowerCase();
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
                throw new InvalidValueException(ErrorCode.UNSUPPORTED_TYPE);
        }
    }
}
