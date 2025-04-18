package com.lshdainty.myhr.service;

import com.lshdainty.myhr.domain.Holiday;
import com.lshdainty.myhr.domain.HolidayType;
import com.lshdainty.myhr.repository.HolidayRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HolidayService {
    private final MessageSource ms;
    private final HolidayRepositoryImpl holidayRepositoryImpl;

    @Transactional
    public Long save(String name, String date, HolidayType type) {
        Holiday holiday = Holiday.createHoliday(name, date, type);
        holidayRepositoryImpl.save(holiday);
        return holiday.getSeq();
    }

    public Holiday findHoliday(Long seq) {
        return checkHolidayExist(seq);
    }

    public List<Holiday> findHolidays() {
        return holidayRepositoryImpl.findHolidays();
    }

    public List<Holiday> findHolidaysByStartEndDate(String startDate, String endDate) {
        return holidayRepositoryImpl.findHolidaysByStartEndDate(startDate, endDate);
    }

    public List<Holiday> findHolidaysByType(HolidayType type) {
        return holidayRepositoryImpl.findHolidaysByType(type);
    }

    @Transactional
    public void editHoliday(Long holidaySeq, String name, String date, HolidayType type) {
        Holiday findHoliday = checkHolidayExist(holidaySeq);
        findHoliday.updateHoliday(name, date, type);
    }

    @Transactional
    public void deleteHoliday(Long holidaySeq) {
        Holiday findHoliday = checkHolidayExist(holidaySeq);
        holidayRepositoryImpl.delete(findHoliday);
    }

    public Holiday checkHolidayExist(Long holidaySeq) {
        Holiday findHoliday = holidayRepositoryImpl.findHoliday(holidaySeq);
        if (Objects.isNull(findHoliday)) { throw new IllegalArgumentException(ms.getMessage("error.notfound.holiday", null, null)); }
        return findHoliday;
    }
}
