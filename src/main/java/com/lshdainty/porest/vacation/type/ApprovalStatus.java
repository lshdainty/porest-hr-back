package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum ApprovalStatus implements DisplayType {
    PENDING("대기중"),
    APPROVED("승인"),
    REJECTED("반려");

    private String strName;

    ApprovalStatus(String strName) {
        this.strName = strName;
    }

    @Override
    public String getViewName() {
        return strName;
    }
}
