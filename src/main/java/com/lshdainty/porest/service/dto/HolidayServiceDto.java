package com.lshdainty.porest.service.dto;

import com.lshdainty.porest.type.CountryCode;
import com.lshdainty.porest.type.HolidayType;
import com.lshdainty.porest.type.YNType;
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
    private CountryCode countryCode;
    private YNType lunarYN;
    private String lunarDate;
    private YNType isRecurring;
    private String icon;
}