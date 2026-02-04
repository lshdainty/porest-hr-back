package com.porest.hr.holiday.service;

import com.porest.core.exception.DuplicateException;
import com.porest.core.exception.EntityNotFoundException;
import com.porest.hr.common.exception.HrErrorCode;
import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.holiday.domain.Holiday;
import com.porest.hr.holiday.repository.HolidayRepository;
import com.porest.hr.holiday.service.dto.HolidayServiceDto;
import com.porest.hr.holiday.type.HolidayType;
import com.github.usingsky.calendar.KoreanLunarCalendar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
        log.info("공휴일 등록 완료: holidayId={}, name={}", holiday.getRowId(), data.getName());
        return holiday.getRowId();
    }

    @Override
    public Holiday findByRowId(Long rowId) {
        log.debug("공휴일 조회: rowId={}", rowId);
        return checkHolidayExist(rowId);
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
        return holidayRepository.findByRowId(holidayId)
                .orElseThrow(() -> {
                    log.warn("공휴일 조회 실패 - 존재하지 않는 공휴일: holidayId={}", holidayId);
                    return new EntityNotFoundException(HrErrorCode.HOLIDAY_NOT_FOUND);
                });
    }

    @Override
    public List<HolidayServiceDto> getRecurringHolidaysPreview(int targetYear, CountryCode countryCode) {
        log.debug("반복 공휴일 프리뷰 조회: targetYear={}, countryCode={}", targetYear, countryCode);

        List<Holiday> recurringHolidays = holidayRepository.findByIsRecurring(YNType.Y, countryCode);

        List<HolidayServiceDto> result = recurringHolidays.stream()
                .map(holiday -> convertToTargetYear(holiday, targetYear))
                .collect(Collectors.toList());

        log.info("반복 공휴일 프리뷰 조회 완료: targetYear={}, count={}", targetYear, result.size());
        return result;
    }

    /**
     * 반복 공휴일을 특정 연도로 변환
     * - 양력: 년도만 변경
     * - 음력: 음력->양력 변환 (양력/음력 년도 차이 고려)
     */
    private HolidayServiceDto convertToTargetYear(Holiday holiday, int targetYear) {
        LocalDate targetDate;
        LocalDate targetLunarDate = null;

        // 음력 공휴일이면서 유효한 음력 날짜가 있는 경우
        if (YNType.isY(holiday.getLunarYN()) && isValidLunarDate(holiday.getLunarDate())) {
            // 양력 년도와 음력 년도의 차이 계산
            // 예: 설날 전날 - 양력 2025-01-28, 음력 2024-12-29 -> yearOffset = 1
            int yearOffset = holiday.getDate().getYear() - holiday.getLunarDate().getYear();
            int targetLunarYear = targetYear - yearOffset;

            // 음력 공휴일: 년도 차이를 반영하여 음력 날짜 설정 후 양력으로 변환
            targetLunarDate = holiday.getLunarDate().withYear(targetLunarYear);
            LocalDate convertedDate = convertLunarToSolar(targetLunarYear, targetLunarDate.getMonthValue(), targetLunarDate.getDayOfMonth());

            // 음력 변환 실패 시 양력 날짜로 폴백
            if (convertedDate != null) {
                targetDate = convertedDate;
            } else {
                log.warn("음력 변환 실패로 양력 날짜 사용: holidayName={}, lunarDate={}", holiday.getName(), targetLunarDate);
                targetDate = holiday.getDate().withYear(targetYear);
                targetLunarDate = null;  // 음력 날짜 무효화
            }
        } else {
            // 양력 공휴일: 년도만 변경
            targetDate = holiday.getDate().withYear(targetYear);
        }

        return HolidayServiceDto.builder()
                .name(holiday.getName())
                .date(targetDate)
                .type(holiday.getType())
                .countryCode(holiday.getCountryCode())
                .lunarYN(holiday.getLunarYN())
                .lunarDate(targetLunarDate)
                .isRecurring(YNType.N)  // 생성된 공휴일은 반복 아님
                .icon(holiday.getIcon())
                .build();
    }

    /**
     * 음력 날짜가 유효한지 검증
     */
    private boolean isValidLunarDate(LocalDate lunarDate) {
        if (lunarDate == null) {
            return false;
        }
        // 년도가 0이거나 유효하지 않은 날짜 체크
        return lunarDate.getYear() > 0;
    }

    /**
     * 음력 -> 양력 변환 (korean-lunar-calendar 라이브러리 사용)
     * @return 변환된 양력 날짜, 변환 실패 시 null
     */
    private LocalDate convertLunarToSolar(int year, int month, int day) {
        KoreanLunarCalendar calendar = KoreanLunarCalendar.getInstance();
        boolean isValid = calendar.setLunarDate(year, month, day, false);

        if (!isValid) {
            log.warn("음력 날짜 변환 실패 - 유효하지 않은 음력 날짜: {}-{}-{}", year, month, day);
            return null;
        }

        String solarIsoFormat = calendar.getSolarIsoFormat(); // "yyyy-MM-dd" 형식

        // 0000-00-00 같은 유효하지 않은 결과 체크
        if (solarIsoFormat == null || solarIsoFormat.startsWith("0000")) {
            log.warn("음력 날짜 변환 실패 - 유효하지 않은 결과: {}", solarIsoFormat);
            return null;
        }

        return LocalDate.parse(solarIsoFormat);
    }

    @Override
    @Transactional
    public int bulkSaveHolidays(List<HolidayServiceDto> holidays) {
        log.debug("공휴일 일괄 저장 시작: count={}", holidays.size());

        if (holidays.isEmpty()) {
            log.info("공휴일 일괄 저장 완료: count=0 (빈 목록)");
            return 0;
        }

        // 중복 체크
        for (HolidayServiceDto data : holidays) {
            if (holidayRepository.existsByDateAndNameAndCountryCode(
                    data.getDate(), data.getName(), data.getCountryCode())) {
                log.warn("중복 공휴일 발견: date={}, name={}, countryCode={}",
                        data.getDate(), data.getName(), data.getCountryCode());
                throw new DuplicateException(HrErrorCode.HOLIDAY_ALREADY_EXISTS);
            }
        }

        // 엔티티 생성 및 저장
        List<Holiday> holidayEntities = holidays.stream()
                .map(data -> Holiday.createHoliday(
                        data.getName(),
                        data.getDate(),
                        data.getType(),
                        data.getCountryCode(),
                        data.getLunarYN(),
                        data.getLunarDate(),
                        data.getIsRecurring(),
                        data.getIcon()
                ))
                .collect(Collectors.toList());

        holidayRepository.saveAll(holidayEntities);
        log.info("공휴일 일괄 저장 완료: count={}", holidayEntities.size());

        return holidayEntities.size();
    }
}
