package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum ApprovalStatus implements DisplayType {
    PENDING("대기중", 1L),
    APPROVED("승인", 2L),
    REJECTED("반려", 3L);

    private String strName;
    private Long orderSeq;

    ApprovalStatus(String strName, Long orderSeq) {
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
