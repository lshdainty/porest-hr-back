package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum RepeatUnit implements DisplayType {
    YEARLY(1L),
    MONTHLY(2L),
    DAILY(3L),
    HALF(4L),
    QUARTERLY(5L);

    private static final String MESSAGE_KEY_PREFIX = "type.repeat.unit.";
    private Long orderSeq;

    RepeatUnit(Long orderSeq) {
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
}
