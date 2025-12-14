package com.lshdainty.porest.holiday.controller;

import com.lshdainty.porest.common.controller.ApiResponse;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.controller.dto.HolidayApiDto;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.service.HolidayService;
import com.lshdainty.porest.holiday.service.dto.HolidayServiceDto;
import com.lshdainty.porest.holiday.type.HolidayType;
import com.lshdainty.porest.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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
        Long holidayId = holidayService.registHoliday(HolidayServiceDto.builder()
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
        return ApiResponse.success(new HolidayApiDto.RegistHolidayResp(holidayId));
    }

    @Override
    @PreAuthorize("hasAuthority('HOLIDAY_READ')")
    public ApiResponse searchHolidaysByStartEndDate(LocalDate start, LocalDate end, CountryCode countryCode, User loginUser) {
        // countryCode가 null이면 로그인 사용자의 국가 코드 사용
        CountryCode targetCountryCode = countryCode != null ? countryCode : loginUser.getCountryCode();

        List<Holiday> holidays = holidayService.searchHolidaysByStartEndDate(start, end, targetCountryCode);

        List<HolidayApiDto.SearchHolidaysResp> resp = holidays.stream()
                .map(h -> new HolidayApiDto.SearchHolidaysResp(
                        h.getId(),
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
                        h.getId(),
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
    public ApiResponse editHoliday(Long id, HolidayApiDto.EditHolidayReq data) {
        holidayService.editHoliday(HolidayServiceDto.builder()
                .id(id)
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

        Holiday findHoliday = holidayService.findById(id);
        return ApiResponse.success(new HolidayApiDto.EditHolidayResp(
                findHoliday.getId(),
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
    public ApiResponse deleteHoliday(Long id) {
        holidayService.deleteHoliday(id);
        return ApiResponse.success();
    }

    @Override
    @PreAuthorize("hasAuthority('HOLIDAY_MANAGE')")
    public ApiResponse previewRecurringHolidays(Integer targetYear, CountryCode countryCode) {
        List<HolidayServiceDto> previews = holidayService.getRecurringHolidaysPreview(targetYear, countryCode);

        List<HolidayApiDto.PreviewRecurringHolidaysResp> resp = previews.stream()
                .map(dto -> new HolidayApiDto.PreviewRecurringHolidaysResp(
                        dto.getName(),
                        dto.getDate(),
                        dto.getType(),
                        dto.getCountryCode(),
                        dto.getLunarYN(),
                        dto.getLunarDate(),
                        dto.getIsRecurring(),
                        dto.getIcon()
                ))
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @Override
    @PreAuthorize("hasAuthority('HOLIDAY_MANAGE')")
    public ApiResponse bulkSaveHolidays(HolidayApiDto.BulkSaveHolidaysReq data) {
        List<HolidayServiceDto> serviceDtos = data.getHolidays().stream()
                .map(item -> HolidayServiceDto.builder()
                        .name(item.getHolidayName())
                        .date(item.getHolidayDate())
                        .type(item.getHolidayType())
                        .countryCode(item.getCountryCode())
                        .lunarYN(item.getLunarYn())
                        .lunarDate(item.getLunarDate())
                        .isRecurring(item.getIsRecurring())
                        .icon(item.getHolidayIcon())
                        .build())
                .collect(Collectors.toList());

        int savedCount = holidayService.bulkSaveHolidays(serviceDtos);

        return ApiResponse.success(new HolidayApiDto.BulkSaveHolidaysResp(savedCount));
    }
}
