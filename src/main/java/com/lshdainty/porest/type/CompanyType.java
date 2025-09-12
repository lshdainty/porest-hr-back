package com.lshdainty.porest.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CompanyType {
    SKAX("SK AX"),
    DTOL("디투엘"),
    INSIGHTON("인사이트온"),
    BIGXDATA("BigxData"),
    CNTHOTH("씨앤토트플러스"),
    AGS("AGS");

    private String companyName;

    CompanyType(String companyName) { this.companyName = companyName; }
}
