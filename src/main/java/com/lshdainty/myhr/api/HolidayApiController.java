package com.lshdainty.myhr.api;

import com.lshdainty.myhr.domain.Holiday;
import com.lshdainty.myhr.domain.HolidayType;
import com.lshdainty.myhr.dto.HolidayDto;
import com.lshdainty.myhr.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HolidayApiController {
    private final HolidayService holidayService;

    @PostMapping("api/v1/holiday")
    public ApiResponse registHoliday(@RequestBody HolidayDto req) {
        Long holidaySeq = holidayService.save(req.getHolidayName(), req.getHolidayDate(), req.getHolidayType());
        return ApiResponse.success(new HolidayDto(holidaySeq));
    }

    @GetMapping("api/v1/holidays/date")
    public ApiResponse getHolidaysByStartEndDate(@RequestParam("start") String start, @RequestParam("end") String end) {
        List<Holiday> holidays = holidayService.findHolidaysByStartEndDate(start, end);

        List<HolidayDto> resp = holidays.stream()
                .map(HolidayDto::new)
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @GetMapping("api/v1/holidays/type/{type}")
    public ApiResponse getHolidaysByType(@PathVariable("type") HolidayType type) {
        List<Holiday> holidays = holidayService.findHolidaysByType(type);

        List<HolidayDto> resp = holidays.stream()
                .map(HolidayDto::new)
                .collect(Collectors.toList());

        return ApiResponse.success(resp);
    }

    @PutMapping("/api/v1/holiday/{seq}")
    public ApiResponse editHoliday(@PathVariable("seq") Long seq, @RequestBody HolidayDto req) {
        holidayService.editHoliday(seq, req.getHolidayName(), req.getHolidayDate(), req.getHolidayType());

        Holiday findHoliday = holidayService.findHoliday(seq);
        return ApiResponse.success(new HolidayDto(findHoliday));
    }

    @DeleteMapping("/api/v1/holiday/{seq}")
    public ApiResponse deleteHoliday(@PathVariable("seq") Long seq) {
        holidayService.deleteHoliday(seq);
        return ApiResponse.success();
    }
}
