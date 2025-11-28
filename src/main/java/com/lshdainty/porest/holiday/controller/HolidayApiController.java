package com.lshdainty.porest.holiday.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.holiday.controller.dto.HolidayApiDto;
import com.lshdainty.porest.holiday.service.HolidayService;
import com.lshdainty.porest.holiday.service.dto.HolidayServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HolidayApiController implements HolidayApi {
    private final HolidayService holidayService;

    @Override
    @PreAuthorize("hasAuthority('HOLIDAY_MANAGE')")
    public ApiResponse registHoliday(HolidayApiDto.RegistHolidayReq data) {
        Long holidaySeq = holidayService.registHoliday(HolidayServiceDto.builder()
                .name(data.getHolidayName())
                .date(data.getHolidayDate())
                .type(data.getHolidayType())
                .countryCode(data.getCountryCode())
                .lunarYN(data.getLunarYn())
                .lunarDate(data.getLunarDate())
                .isRecurring(data.getIsRecurring())
                .icon(data.getHolidayIcon())
                .build()
        );
        return ApiResponse.success(new HolidayApiDto.RegistHolidayResp(holidaySeq));
    }

    @Override
    @PreAuthorize("hasAuthority('HOLIDAY_READ')")
    public ApiResponse searchHolidaysByStartEndDate(String start, String end, CountryCode countryCode) {
        List<Holiday> holidays = holidayService.searchHolidaysByStartEndDate(start, end, countryCode);

        List<HolidayApiDto.SearchHolidaysResp> resp = holidays.stream()
                .map(h -> new HolidayApiDto.SearchHolidaysResp(
                        h.getSeq(),
                        h.getName(),
                        h.getDate(),
                        h.getType(),
                        h.getCountryCode(),
                        h.getLunarYN(),
                        h.getLunarDate(),
                        h.getIsRecurring(),
                        h.getIcon()
                ))
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('HOLIDAY_READ')")
    public ApiResponse searchHolidaysByType(HolidayType type) {
        List<Holiday> holidays = holidayService.searchHolidaysByType(type);

        List<HolidayApiDto.SearchHolidaysResp> resp = holidays.stream()
                .map(h -> new HolidayApiDto.SearchHolidaysResp(
                        h.getSeq(),
                        h.getName(),
                        h.getDate(),
                        h.getType(),
                        h.getCountryCode(),
                        h.getLunarYN(),
                        h.getLunarDate(),
                        h.getIsRecurring(),
                        h.getIcon()
                ))
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('HOLIDAY_MANAGE')")
    public ApiResponse editHoliday(Long seq, HolidayApiDto.EditHolidayReq data) {
        holidayService.editHoliday(HolidayServiceDto.builder()
                .seq(seq)
                .name(data.getHolidayName())
                .date(data.getHolidayDate())
                .type(data.getHolidayType())
                .countryCode(data.getCountryCode())
                .lunarYN(data.getLunarYn())
                .lunarDate(data.getLunarDate())
                .isRecurring(data.getIsRecurring())
                .icon(data.getHolidayIcon())
                .build()
        );

        Holiday findHoliday = holidayService.findById(seq);
        return ApiResponse.success(new HolidayApiDto.EditHolidayResp(
                findHoliday.getSeq(),
                findHoliday.getName(),
                findHoliday.getDate(),
                findHoliday.getType(),
                findHoliday.getCountryCode(),
                findHoliday.getLunarYN(),
                findHoliday.getLunarDate(),
                findHoliday.getIsRecurring(),
                findHoliday.getIcon()
        ));
    }

    @Override
    @PreAuthorize("hasAuthority('HOLIDAY_MANAGE')")
    public ApiResponse deleteHoliday(Long seq) {
        holidayService.deleteHoliday(seq);
        return ApiResponse.success();
    }
}
