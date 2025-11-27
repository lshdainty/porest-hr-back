package com.lshdainty.porest.holiday.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum HolidayType implements DisplayType {
    PUBLIC("공휴일", 1L),
    SUBSTITUTE("대체", 2L),
    ETC("기타", 3L);

    private String strName;
    private Long orderSeq;

    HolidayType(String strName, Long orderSeq) {
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