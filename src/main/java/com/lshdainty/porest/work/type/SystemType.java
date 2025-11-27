package com.lshdainty.porest.work.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum SystemType implements DisplayType {
    SYSTEM1("system1", 1L);

    private String strName;
    private Long orderSeq;

    SystemType(String strName, Long orderSeq) {
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
