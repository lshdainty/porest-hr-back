package com.lshdainty.porest.holiday.service;

import com.lshdainty.porest.common.message.MessageKey;
import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.util.MessageResolver;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HolidayService {
    private final MessageResolver messageResolver;
    private final HolidayRepository holidayRepository;

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
        log.info("공휴일 등록 완료: holidaySeq={}, name={}", holiday.getSeq(), data.getName());
        return holiday.getSeq();
    }

    public Holiday findById(Long seq) {
        log.debug("공휴일 조회: seq={}", seq);
        return checkHolidayExist(seq);
    }

    public List<Holiday> findHolidays(CountryCode countryCode) {
        log.debug("공휴일 목록 조회: countryCode={}", countryCode);
        return holidayRepository.findHolidays(countryCode);
    }

    public List<Holiday> searchHolidaysByStartEndDate(LocalDate startDate, LocalDate endDate, CountryCode countryCode) {
        log.debug("기간별 공휴일 조회: startDate={}, endDate={}, countryCode={}", startDate, endDate, countryCode);
        return holidayRepository.findHolidaysByStartEndDate(startDate, endDate, countryCode);
    }

    public List<Holiday> searchHolidaysByType(HolidayType type) {
        log.debug("타입별 공휴일 조회: type={}", type);
        return holidayRepository.findHolidaysByType(type);
    }

    @Transactional
    public void editHoliday(HolidayServiceDto data) {
        log.debug("공휴일 수정 시작: holidaySeq={}", data.getSeq());
        Holiday findHoliday = checkHolidayExist(data.getSeq());
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
        log.info("공휴일 수정 완료: holidaySeq={}", data.getSeq());
    }

    @Transactional
    public void deleteHoliday(Long holidaySeq) {
        log.debug("공휴일 삭제 시작: holidaySeq={}", holidaySeq);
        Holiday findHoliday = checkHolidayExist(holidaySeq);
        holidayRepository.delete(findHoliday);
        log.info("공휴일 삭제 완료: holidaySeq={}", holidaySeq);
    }

    public Holiday checkHolidayExist(Long holidaySeq) {
        Optional<Holiday> holiday = holidayRepository.findById(holidaySeq);
        holiday.orElseThrow(() -> {
            log.warn("공휴일 조회 실패 - 존재하지 않는 공휴일: holidaySeq={}", holidaySeq);
            return new IllegalArgumentException(messageResolver.getMessage(MessageKey.NOT_FOUND_HOLIDAY));
        });
        return holiday.get();
    }
}
