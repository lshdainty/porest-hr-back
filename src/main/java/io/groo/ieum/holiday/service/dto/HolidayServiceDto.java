package io.groo.ieum.holiday.service.dto;

import io.groo.core.type.CountryCode;
import io.groo.ieum.holiday.type.HolidayType;
import io.groo.core.type.YNType;
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