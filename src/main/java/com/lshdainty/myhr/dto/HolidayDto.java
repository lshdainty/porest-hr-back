package com.lshdainty.myhr.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.domain.Holiday;
import com.lshdainty.myhr.domain.HolidayType;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HolidayDto {
    private Long holidaySeq;
    private String holidayName;
    private String holidayDate;
    private HolidayType holidayType;

    public HolidayDto(Long seq) {
        this.holidaySeq = seq;
    }

    public HolidayDto(Holiday holiday) {
        this.holidaySeq = holiday.getSeq();
        this.holidayName = holiday.getName();
        this.holidayDate = holiday.getDate();
        this.holidayType = holiday.getType();
    }
}
