package com.lshdainty.porest.type.vacation;

public enum VacationType {
    ANNUAL("연차"),
    MATERNITY("출산"),
    WEDDING("결혼"),
    BEREAVEMENT("상조"),
    OVERTIME("연장"),
    HEALTH("건강"),
    ARMY("군");

    private String strName;

    VacationType(String strName) {
        this.strName = strName;
    }

    public String getStrName() {return strName;}
}