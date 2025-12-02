package com.lshdainty.porest.holiday.repository;

import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.type.HolidayType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HolidayRepository {
    // 신규 휴일 저장
    void save(Holiday holiday);
    // 단건 휴일 조회
    Optional<Holiday> findById(Long seq);
    // 전체 휴일 조회
    List<Holiday> findHolidays(CountryCode countryCode);
    // 기간에 해당하는 휴일 조회
    List<Holiday> findHolidaysByStartEndDate(LocalDate start, LocalDate end, CountryCode countryCode);
    // 휴일 타입에 따른 조회
    List<Holiday> findHolidaysByType(HolidayType type);
    // 기간과 타입에 해당하는 휴일 조회
    List<Holiday> findHolidaysByStartEndDateWithType(LocalDate start, LocalDate end, HolidayType type);
    // 휴일 삭제
    void delete(Holiday holiday);
}
