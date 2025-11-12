package com.lshdainty.porest.schedule.type;

import com.lshdainty.porest.common.type.DisplayType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleType implements DisplayType {
    EDUCATION("교육"),
    BIRTHDAY("생일"),
    BUSINESSTRIP("출장"),
    BIRTHPARTY("생일파티");

    private String strName;

    ScheduleType(String typeName) {
        this.strName = typeName;
    }

    @Override
    public String getViewName() {return this.strName;}
}
