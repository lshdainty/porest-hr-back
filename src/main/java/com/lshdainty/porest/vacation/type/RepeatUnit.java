package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum RepeatUnit implements DisplayType {
    YEARLY("매년", 1L),
    MONTHLY("매월", 2L),
    DAILY("매일", 3L),
    HALF("반기", 4L),
    QUARTERLY("분기", 5L);

    private String strName;
    private Long orderSeq;

    RepeatUnit(String strName, Long orderSeq) {
        this.strName = strName;
        this.orderSeq = orderSeq;
    }

    @Override
    public String getViewName() {
        return strName;
    }

    @Override
    public Long getOrderSeq() {
        return orderSeq;
    }
}
