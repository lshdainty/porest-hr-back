package com.lshdainty.porest.type.vacation;

public enum GrantMethod {
    ON_REQUEST("신청시 부여"),
    MANUAL_GRANT("관리자 직접 부여"),
    REPEAT_GRANT("반복 부여");

    private String strName;

    GrantMethod(String strName) {
        this.strName = strName;
    }

    public String getStrName() {return strName;}
};