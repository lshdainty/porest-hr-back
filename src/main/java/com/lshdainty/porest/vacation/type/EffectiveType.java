package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.exception.InvalidValueException;
import com.lshdainty.porest.common.type.DisplayType;

import java.time.LocalDateTime;

public enum EffectiveType implements DisplayType {
    IMMEDIATELY(1L),
    START_OF_YEAR(2L);

    private static final String MESSAGE_KEY_PREFIX = "type.effective.type.";
    private Long orderSeq;

    EffectiveType(Long orderSeq) {
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
                throw new InvalidValueException(ErrorCode.UNSUPPORTED_TYPE);
        }
    }
}
