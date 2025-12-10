package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum GrantMethod implements DisplayType {
    ON_REQUEST(1L),
    MANUAL_GRANT(2L),
    REPEAT_GRANT(3L);

    private static final String MESSAGE_KEY_PREFIX = "type.grant.method.";
    private Long orderSeq;

    GrantMethod(Long orderSeq) {
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