package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum VacationType implements DisplayType {
    ANNUAL("연차", 1L),
    MATERNITY("출산", 2L),
    WEDDING("결혼", 3L),
    BEREAVEMENT("상조", 4L),
    OVERTIME("연장", 5L),
    HEALTH("건강", 6L),
    ARMY("군", 7L);

    private String strName;
    private Long orderSeq;

    VacationType(String strName, Long orderSeq) {
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