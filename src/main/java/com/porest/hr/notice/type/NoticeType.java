package com.porest.hr.notice.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum NoticeType implements DisplayType {
    GENERAL(1L),
    URGENT(2L),
    EVENT(3L),
    MAINTENANCE(4L);

    private static final String MESSAGE_KEY_PREFIX = "type.notice.type.";
    private Long orderSeq;

    NoticeType(Long orderSeq) {
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
