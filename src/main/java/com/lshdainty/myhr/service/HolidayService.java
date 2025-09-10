package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.Holiday;
import com.lshdainty.myhr.type.CountryCode;
import com.lshdainty.myhr.type.HolidayType;
import com.lshdainty.myhr.repository.HolidayRepositoryImpl;
import com.lshdainty.myhr.service.dto.HolidayServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HolidayService {
    private final MessageSource ms;
    private final HolidayRepositoryImpl holidayRepositoryImpl;

    @Transactional
    public Long save(HolidayServiceDto data) {
        Holiday holiday = Holiday.createHoliday(
                data.getName(),
                data.getDate(),
                data.getType(),
                data.getCountryCode(),
                data.getLunarYN(),
                data.getLunarDate(),
                data.getIsRecurring()
        );
        holidayRepositoryImpl.save(holiday);
        return holiday.getSeq();
    }

    public Holiday findById(Long seq) {
        return checkHolidayExist(seq);
    }

    public List<Holiday> findHolidays(CountryCode countryCode) {
        return holidayRepositoryImpl.findHolidays(countryCode);
    }

    public List<Holiday> findHolidaysByStartEndDate(String startDate, String endDate, CountryCode countryCode) {
        return holidayRepositoryImpl.findHolidaysByStartEndDate(startDate, endDate, countryCode);
    }

    public List<Holiday> findHolidaysByType(HolidayType type) {
        return holidayRepositoryImpl.findHolidaysByType(type);
    }

    @Transactional
    public void editHoliday(HolidayServiceDto data) {
        Holiday findHoliday = checkHolidayExist(data.getSeq());
        findHoliday.updateHoliday(
                data.getName(),
                data.getDate(),
                data.getType(),
                data.getCountryCode(),
                data.getLunarYN(),
                data.getLunarDate(),
                data.getIsRecurring()
        );
    }

    @Transactional
    public void deleteHoliday(Long holidaySeq) {
        Holiday findHoliday = checkHolidayExist(holidaySeq);
        holidayRepositoryImpl.delete(findHoliday);
    }

    public Holiday checkHolidayExist(Long holidaySeq) {
        Optional<Holiday> holiday = holidayRepositoryImpl.findById(holidaySeq);
        holiday.orElseThrow(() -> new IllegalArgumentException(ms.getMessage("error.notfound.holiday", null, null)));
        return holiday.get();
    }
}
