package com.lshdainty.myhr.api;

import com.lshdainty.myhr.domain.Holiday;
import com.lshdainty.myhr.type.CountryCode;
import com.lshdainty.myhr.type.HolidayType;
import com.lshdainty.myhr.api.dto.HolidayDto;
import com.lshdainty.myhr.service.HolidayService;
import com.lshdainty.myhr.service.dto.HolidayServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HolidayApiController {
    private final HolidayService holidayService;

    @PostMapping("api/v1/holiday")
    public ApiResponse registHoliday(@RequestBody HolidayDto data) {
        Long holidaySeq = holidayService.save(HolidayServiceDto.builder()
                .name(data.getHolidayName())
                .date(data.getHolidayDate())
                .type(data.getHolidayType())
                .countryCode(data.getCountryCode())
                .lunarYN(data.getLunarYN())
                .lunarDate(data.getLunarDate())
                .isRecurring(data.getIsRecurring())
                .build()
        );
        return ApiResponse.success(HolidayDto.builder().holidaySeq(holidaySeq).build());
    }

    @GetMapping("api/v1/holidays/date")
    public ApiResponse getHolidaysByStartEndDate(@RequestParam("start") String start, @RequestParam("end") String end, @RequestParam("country_code") CountryCode countryCode) {
        List<Holiday> holidays = holidayService.findHolidaysByStartEndDate(start, end, countryCode);

        List<HolidayDto> resp = holidays.stream()
                .map(h -> HolidayDto.builder()
                        .holidaySeq(h.getSeq())
                        .holidayName(h.getName())
                        .holidayDate(h.getDate())
                        .holidayType(h.getType())
                        .countryCode(h.getCountryCode())
                        .lunarYN(h.getLunarYN())
                        .lunarDate(h.getLunarDate())
                        .isRecurring(h.getIsRecurring())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @GetMapping("api/v1/holidays/type/{type}")
    public ApiResponse getHolidaysByType(@PathVariable("type") HolidayType type) {
        List<Holiday> holidays = holidayService.findHolidaysByType(type);

        List<HolidayDto> resp = holidays.stream()
                .map(h -> HolidayDto.builder()
                        .holidaySeq(h.getSeq())
                        .holidayName(h.getName())
                        .holidayDate(h.getDate())
                        .holidayType(h.getType())
                        .countryCode(h.getCountryCode())
                        .lunarYN(h.getLunarYN())
                        .lunarDate(h.getLunarDate())
                        .isRecurring(h.getIsRecurring())
                        .build())
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @PutMapping("/api/v1/holiday/{seq}")
    public ApiResponse editHoliday(@PathVariable("seq") Long seq, @RequestBody HolidayDto data) {
        holidayService.editHoliday(HolidayServiceDto.builder()
                .seq(seq)
                .name(data.getHolidayName())
                .date(data.getHolidayDate())
                .type(data.getHolidayType())
                .countryCode(data.getCountryCode())
                .lunarYN(data.getLunarYN())
                .lunarDate(data.getLunarDate())
                .isRecurring(data.getIsRecurring())
                .build()
        );

        Holiday findHoliday = holidayService.findById(seq);
        return ApiResponse.success(HolidayDto.builder()
                .holidaySeq(findHoliday.getSeq())
                .holidayName(findHoliday.getName())
                .holidayDate(findHoliday.getDate())
                .holidayType(findHoliday.getType())
                .countryCode(findHoliday.getCountryCode())
                .lunarYN(findHoliday.getLunarYN())
                .lunarDate(findHoliday.getLunarDate())
                .isRecurring(findHoliday.getIsRecurring())
                .build());
    }

    @DeleteMapping("/api/v1/holiday/{seq}")
    public ApiResponse deleteHoliday(@PathVariable("seq") Long seq) {
        holidayService.deleteHoliday(seq);
        return ApiResponse.success();
    }
}
