package com.lshdainty.porest.type.vacation;

public enum GrantTiming {
    FIXED_DATE("고정 날짜"),
    SPECIFIC_MONTH("특정 월"),
    SPECIFIC_DAY("특정 일"),
    QUARTER_END("분기말"),
    HALF_END("반기말"),
    YEAR_END("연말");

    private String strName;

    GrantTiming(String strName) {
        this.strName = strName;
    }
    public String getStrName() {return strName;}
}
