package com.porest.hr.holiday.type;

import com.porest.core.type.DisplayType;

public enum HolidayType implements DisplayType {
    PUBLIC(1L),
    SUBSTITUTE(2L),
    ETC(3L);

    private static final String MESSAGE_KEY_PREFIX = "type.holiday.type.";
    private Long orderSeq;

    HolidayType(Long orderSeq) {
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