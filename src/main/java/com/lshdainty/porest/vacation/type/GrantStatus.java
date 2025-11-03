package com.lshdainty.porest.vacation.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum GrantStatus implements DisplayType {
    PENDING_APPROVAL("승인대기"),
    ACTIVE("활성"),
    EXHAUSTED("소진"),
    EXPIRED("만료"),
    REVOKED("회수"),
    REJECTED("거부");

    private String strName;

    GrantStatus(String strName) {
        this.strName = strName;
    }

    @Override
    public String getViewName() {return strName;}
}
