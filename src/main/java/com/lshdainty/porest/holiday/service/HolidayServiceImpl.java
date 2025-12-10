package com.lshdainty.porest.holiday.service;

import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.holiday.domain.Holiday;
import com.lshdainty.porest.holiday.repository.HolidayRepository;
import com.lshdainty.porest.holiday.service.dto.HolidayServiceDto;
import com.lshdainty.porest.holiday.type.HolidayType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HolidayServiceImpl implements HolidayService {
    private final HolidayRepository holidayRepository;

    @Override
    @Transactional
    public Long registHoliday(HolidayServiceDto data) {
        log.debug("공휴일 등록 시작: name={}, date={}, type={}", data.getName(), data.getDate(), data.getType());
        Holiday holiday = Holiday.createHoliday(
                data.getName(),
                data.getDate(),
                data.getType(),
                data.getCountryCode(),
                data.getLunarYN(),
                data.getLunarDate(),
                data.getIsRecurring(),
                data.getIcon()
        );
        holidayRepository.save(holiday);
        log.info("공휴일 등록 완료: holidayId={}, name={}", holiday.getId(), data.getName());
        return holiday.getId();
    }

    @Override
    public Holiday findById(Long id) {
        log.debug("공휴일 조회: id={}", id);
        return checkHolidayExist(id);
    }

    @Override
    public List<Holiday> findHolidays(CountryCode countryCode) {
        log.debug("공휴일 목록 조회: countryCode={}", countryCode);
        return holidayRepository.findHolidays(countryCode);
    }

    @Override
    public List<Holiday> searchHolidaysByStartEndDate(LocalDate startDate, LocalDate endDate, CountryCode countryCode) {
        log.debug("기간별 공휴일 조회: startDate={}, endDate={}, countryCode={}", startDate, endDate, countryCode);
        return holidayRepository.findHolidaysByStartEndDate(startDate, endDate, countryCode);
    }

    @Override
    public List<Holiday> searchHolidaysByType(HolidayType type) {
        log.debug("타입별 공휴일 조회: type={}", type);
        return holidayRepository.findHolidaysByType(type);
    }

    @Override
    @Transactional
    public void editHoliday(HolidayServiceDto data) {
        log.debug("공휴일 수정 시작: holidayId={}", data.getId());
        Holiday findHoliday = checkHolidayExist(data.getId());
        findHoliday.updateHoliday(
                data.getName(),
                data.getDate(),
                data.getType(),
                data.getCountryCode(),
                data.getLunarYN(),
                data.getLunarDate(),
                data.getIsRecurring(),
                data.getIcon()
        );
        log.info("공휴일 수정 완료: holidayId={}", data.getId());
    }

    @Override
    @Transactional
    public void deleteHoliday(Long holidayId) {
        log.debug("공휴일 삭제 시작: holidayId={}", holidayId);
        Holiday findHoliday = checkHolidayExist(holidayId);
        holidayRepository.delete(findHoliday);
        log.info("공휴일 삭제 완료: holidayId={}", holidayId);
    }

    @Override
    public Holiday checkHolidayExist(Long holidayId) {
        return holidayRepository.findById(holidayId)
                .orElseThrow(() -> {
                    log.warn("공휴일 조회 실패 - 존재하지 않는 공휴일: holidayId={}", holidayId);
                    return new EntityNotFoundException(ErrorCode.HOLIDAY_NOT_FOUND);
                });
    }
}
