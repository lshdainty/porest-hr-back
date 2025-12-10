package com.lshdainty.porest.schedule.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum ScheduleType implements DisplayType {
    EDUCATION(1L),
    BIRTHDAY(2L),
    BUSINESSTRIP(3L),
    BIRTHPARTY(4L);

    private static final String MESSAGE_KEY_PREFIX = "type.schedule.type.";
    private Long orderSeq;

    ScheduleType(Long orderSeq) {
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
