package com.lshdainty.porest.type.vacation;

public enum RepeatUnit {
    YEARLY("매년"),
    MONTHLY("매월"),
    DAYLY("매일"),
    HALF("반기"),
    QUARTERLY("분기");

    private String strName;

    RepeatUnit(String strName) {
        this.strName = strName;
    }
    public String getStrName() {return strName;}
}
