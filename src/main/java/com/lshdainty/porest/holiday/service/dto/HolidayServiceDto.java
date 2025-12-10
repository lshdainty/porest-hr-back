package com.lshdainty.porest.holiday.service.dto;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.common.type.YNType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@Builder
public class HolidayServiceDto {
    private Long id;
    private String name;
    private LocalDate date;
    private HolidayType type;
    private CountryCode countryCode;
    private YNType lunarYN;
    private LocalDate lunarDate;
    private YNType isRecurring;
    private String icon;
}