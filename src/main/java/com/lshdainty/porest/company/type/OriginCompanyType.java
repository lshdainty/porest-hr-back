package com.lshdainty.porest.company.type;

import com.lshdainty.porest.common.type.DisplayType;

public enum OriginCompanyType implements DisplayType {
    SKAX("SK AX", 1L),
    DTOL("디투엘", 2L),
    INSIGHTON("인사이트온", 3L),
    BIGXDATA("BigxData", 4L),
    CNTHOTH("씨앤토트플러스", 5L),
    AGS("AGS", 6L);

    private String companyName;
    private Long orderSeq;

    OriginCompanyType(String companyName, Long orderSeq) {
        this.companyName = companyName;
        this.orderSeq = orderSeq;
    }

    public String getCompanyName() {
        return companyName;
    }

    @Override
    public String getViewName() {
        return companyName;
    }

    @Override
    public Long getOrderSeq() {
        return orderSeq;
    }
}
