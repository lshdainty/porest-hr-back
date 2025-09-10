package com.lshdainty.myhr.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.lshdainty.myhr.type.CountryCode;
import com.lshdainty.myhr.type.HolidayType;
import com.lshdainty.myhr.type.YNType;
import lombok.*;

@Getter @Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HolidayDto {
    private Long holidaySeq;
    private String holidayName;
    private String holidayDate;
    private HolidayType holidayType;
    private CountryCode countryCode;
    private YNType lunarYN;
    private String lunarDate;
    private YNType isRecurring;
}
