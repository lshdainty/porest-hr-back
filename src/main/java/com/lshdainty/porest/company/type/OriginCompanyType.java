package com.lshdainty.porest.company.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum OriginCompanyType implements DisplayType {
    SKAX(1L),
    DTOL(2L),
    INSIGHTON(3L),
    BIGXDATA(4L),
    CNTHOTH(5L),
    AGS(6L);

    private static final String MESSAGE_KEY_PREFIX = "type.origin.company.";
    private Long orderSeq;

    OriginCompanyType(Long orderSeq) {
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
