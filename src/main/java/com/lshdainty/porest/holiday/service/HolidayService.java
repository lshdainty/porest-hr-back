package com.lshdainty.porest.holiday.service;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.service.dto.HolidayServiceDto;
import com.lshdainty.porest.holiday.type.HolidayType;

import java.time.LocalDate;
import java.util.List;

/**
 * 공휴일 서비스 인터페이스
 * 공휴일 등록, 조회, 수정, 삭제 기능을 제공합니다.
 */
public interface HolidayService {

    /**
     * 공휴일을 등록합니다.
     *
     * @param data 공휴일 등록 데이터
     * @return 등록된 공휴일 ID
     */
    Long registHoliday(HolidayServiceDto data);

    /**
     * ID로 공휴일을 조회합니다.
     *
     * @param id 공휴일 ID
     * @return 공휴일 엔티티
     */
    Holiday findById(Long id);

    /**
     * 국가 코드로 공휴일 목록을 조회합니다.
     *
     * @param countryCode 국가 코드
     * @return 공휴일 목록
     */
    List<Holiday> findHolidays(CountryCode countryCode);

    /**
     * 시작일과 종료일 기간 내의 공휴일을 조회합니다.
     *
     * @param startDate 시작일
     * @param endDate 종료일
     * @param countryCode 국가 코드
     * @return 기간 내 공휴일 목록
     */
    List<Holiday> searchHolidaysByStartEndDate(LocalDate startDate, LocalDate endDate, CountryCode countryCode);

    /**
     * 공휴일 타입으로 공휴일 목록을 조회합니다.
     *
     * @param type 공휴일 타입
     * @return 해당 타입의 공휴일 목록
     */
    List<Holiday> searchHolidaysByType(HolidayType type);

    /**
     * 공휴일 정보를 수정합니다.
     *
     * @param data 수정할 공휴일 데이터
     */
    void editHoliday(HolidayServiceDto data);

    /**
     * 공휴일을 삭제합니다.
     *
     * @param holidayId 삭제할 공휴일 ID
     */
    void deleteHoliday(Long holidayId);

    /**
     * 공휴일 존재 여부를 확인하고 엔티티를 반환합니다.
     *
     * @param holidayId 확인할 공휴일 ID
     * @return 공휴일 엔티티
     * @throws com.lshdainty.porest.common.exception.EntityNotFoundException 공휴일이 존재하지 않을 경우
     */
    Holiday checkHolidayExist(Long holidayId);

    /**
     * 반복 공휴일을 특정 연도로 변환한 프리뷰 목록을 조회합니다.
     *
     * @param targetYear 생성할 연도
     * @param countryCode 국가 코드
     * @return 변환된 공휴일 프리뷰 목록
     */
    List<HolidayServiceDto> getRecurringHolidaysPreview(int targetYear, CountryCode countryCode);

    /**
     * 공휴일을 일괄 저장합니다.
     *
     * @param holidays 저장할 공휴일 목록
     * @return 저장된 공휴일 수
     * @throws com.lshdainty.porest.common.exception.DuplicateException 중복된 공휴일이 존재할 경우
     */
    int bulkSaveHolidays(List<HolidayServiceDto> holidays);
}
