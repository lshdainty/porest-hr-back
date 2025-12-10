package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum VacationType implements DisplayType {
    ANNUAL(1L),
    MATERNITY(2L),
    WEDDING(3L),
    BEREAVEMENT(4L),
    OVERTIME(5L),
    HEALTH(6L),
    ARMY(7L);

    private static final String MESSAGE_KEY_PREFIX = "type.vacation.type.";
    private Long orderSeq;

    VacationType(Long orderSeq) {
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
