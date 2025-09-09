package com.lshdainty.myhr.service.dto;

import com.lshdainty.myhr.type.HolidayType;
import com.lshdainty.myhr.type.YNType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class HolidayServiceDto {
    private Long seq;
    private String name;
    private String date;
    private HolidayType type;
    private String countryCode;
    private YNType lunarYN;
    private YNType isRecurring;
}