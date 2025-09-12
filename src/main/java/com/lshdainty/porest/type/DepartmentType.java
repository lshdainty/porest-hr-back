package com.lshdainty.porest.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DepartmentType {
    SKC("SKC"),
    GMES("G-MES"),
    GSCM("G-SCM"),
    CMP("CMP MES"),
    OLIVE("OLIVE"),
    MYDATA("myDATA"),
    TABLEAU("Tableau"),
    AOI("AOI");

    private String departmentName;

    DepartmentType(String departmentName) { this.departmentName = departmentName; }
}