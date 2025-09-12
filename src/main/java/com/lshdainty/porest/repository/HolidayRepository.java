package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Holiday;
import com.lshdainty.porest.type.CountryCode;
import com.lshdainty.porest.type.HolidayType;

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
    List<Holiday> findHolidaysByStartEndDate(String start, String end, CountryCode countryCode);
    // 휴일 타입에 따른 조회
    List<Holiday> findHolidaysByType(HolidayType type);
    // 기간과 타입에 해당하는 휴일 조회
    List<Holiday> findHolidaysByStartEndDateWithType(String start, String end, HolidayType type);
    // 휴일 삭제
    void delete(Holiday holiday);
}
