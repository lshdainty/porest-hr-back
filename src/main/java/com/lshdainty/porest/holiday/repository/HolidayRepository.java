package com.lshdainty.porest.holiday.repository;

import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.type.HolidayType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Holiday Repository Interface
 */
public interface HolidayRepository {
    /**
     * 신규 휴일 저장
     *
     * @param holiday 저장할 휴일
     */
    void save(Holiday holiday);

    /**
     * 단건 휴일 조회
     *
     * @param id 휴일 ID
     * @return Optional&lt;Holiday&gt;
     */
    Optional<Holiday> findById(Long id);

    /**
     * 전체 휴일 조회
     *
     * @param countryCode 국가 코드
     * @return List&lt;Holiday&gt;
     */
    List<Holiday> findHolidays(CountryCode countryCode);

    /**
     * 기간에 해당하는 휴일 조회
     *
     * @param start 조회 시작일
     * @param end 조회 종료일
     * @param countryCode 국가 코드
     * @return List&lt;Holiday&gt;
     */
    List<Holiday> findHolidaysByStartEndDate(LocalDate start, LocalDate end, CountryCode countryCode);

    /**
     * 휴일 타입에 따른 조회
     *
     * @param type 휴일 타입
     * @return List&lt;Holiday&gt;
     */
    List<Holiday> findHolidaysByType(HolidayType type);

    /**
     * 기간과 타입에 해당하는 휴일 조회
     *
     * @param start 조회 시작일
     * @param end 조회 종료일
     * @param type 휴일 타입
     * @param countryCode 국가 코드
     * @return List&lt;Holiday&gt;
     */
    List<Holiday> findHolidaysByStartEndDateWithType(LocalDate start, LocalDate end, HolidayType type, CountryCode countryCode);

    /**
     * 휴일 삭제
     *
     * @param holiday 삭제할 휴일
     */
    void delete(Holiday holiday);
}
