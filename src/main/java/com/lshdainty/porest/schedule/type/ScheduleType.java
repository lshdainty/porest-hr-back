package com.lshdainty.porest.schedule.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum ScheduleType implements DisplayType {
    EDUCATION("교육", 1L),
    BIRTHDAY("생일", 2L),
    BUSINESSTRIP("출장", 3L),
    BIRTHPARTY("생일파티", 4L);

    private String strName;
    private Long orderSeq;

    ScheduleType(String strName, Long orderSeq) {
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
