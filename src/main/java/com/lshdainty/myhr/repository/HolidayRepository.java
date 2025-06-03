package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.Holiday;
import com.lshdainty.myhr.domain.HolidayType;

import java.util.List;

public interface HolidayRepository {
    void save(Holiday holiday);
    Holiday findHoliday(Long holidaySeq);
    List<Holiday> findHolidays();
    List<Holiday> findHolidaysByStartEndDate(String start, String end);
    List<Holiday> findHolidaysByType(HolidayType type);
    List<Holiday> findHolidaysByStartEndDateWithType(String start, String end, HolidayType type);
    void delete(Holiday holiday);
}
