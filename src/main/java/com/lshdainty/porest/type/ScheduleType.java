package com.lshdainty.porest.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleType {
    EDUCATION("교육"),
    BIRTHDAY("생일"),
    BUSINESSTRIP("출장"),
    DEFENSE("민방위"),
    DEFENSEHALF("민방위(반차)"),
    HEALTHCHECKHALF("건강검진(반차)"),
    BIRTHPARTY("생일파티");

    private String strName;

    ScheduleType(String typeName) {
        this.strName = typeName;
    }
}
