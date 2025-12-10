package com.lshdainty.porest.work.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum SystemType implements DisplayType {
    ERP(1L),
    MES(2L),
    WMS(3L),
    SCM(4L),
    CRM(5L),
    HRM(6L),
    FINANCE(7L),
    PROJECT(8L),
    QUALITY(9L),
    SALES(10L),
    PURCHASE(11L),
    ASSET(12L),
    BI(13L),
    PORTAL(14L),
    GROUPWARE(15L),
    ETC(99L);

    private static final String MESSAGE_KEY_PREFIX = "type.system.type.";
    private Long orderSeq;

    SystemType(Long orderSeq) {
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
