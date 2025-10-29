package com.lshdainty.porest.vacation.service.policy;

import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.type.GrantTiming;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Objects;

@RequiredArgsConstructor
public class RepeatGrant implements VacationPolicyStrategy {
    private final MessageSource ms;
    private final VacationPolicyCustomRepositoryImpl vacationPolicyRepository;

    @Override
    public Long registVacationPolicy(VacationPolicyServiceDto data) {
        // 반복 휴가 정책의 검증
        validateRepeatGrantPolicy(data);

        VacationPolicy vacationPolicy = VacationPolicy.createVacationPolicy(
                data.getName(),
                data.getDesc(),
                data.getVacationType(),
                data.getGrantMethod(),
                data.getGrantTime(),
                data.getRepeatUnit(),
                data.getRepeatInterval(),
                data.getGrantTiming(),
                data.getSpecificMonths(),
                data.getSpecificDays(),
                data.getFirstGrantDate()
        );

        vacationPolicyRepository.save(vacationPolicy);
        return vacationPolicy.getId();
    }

    /**
     * 반복 부여 방식의 휴가 정책 검증
     * 1. 정책명 필수 검증
     * 2. 정책명 중복 검증
     * 3. 부여시간 필수 및 양수 검증
     * 4. 반복 단위 필수 검증
     * 5. 반복 간격 필수 및 양수 검증
     * 6. 부여 시점 지정 방식 필수 검증
     * 7. 부여 시점에 따른 특정 월/일 검증
     * 8. 첫 부여 시점 필수 검증
     *
     * @param data 휴가 정책 데이터
     */
    private void validateRepeatGrantPolicy(VacationPolicyServiceDto data) {
        // 1. 정책명 필수 검증
        if (Objects.isNull(data.getName()) || data.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.name.required", null, null));
        }

        // 2. 정책명 중복 검증
        if (vacationPolicyRepository.existsByName(data.getName())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.name.duplicate", null, null));
        }

        // 3. 부여시간 필수 검증 (스케줄러에서 휴가 부여를 위해 필수)
        if (Objects.isNull(data.getGrantTime())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTime.required", null, null));
        }

        // 4. 부여시간은 0보다 커야 함
        if (data.getGrantTime().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTime.positive", null, null));
        }

        // 5. 반복 단위 필수 검증
        if (Objects.isNull(data.getRepeatUnit())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.repeatUnit.required", null, null));
        }

        // 6. 반복 간격 필수 및 양수 검증
        if (Objects.isNull(data.getRepeatInterval()) || data.getRepeatInterval() <= 0) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.repeatInterval.positive", null, null));
        }

        // 7. 부여 시점 지정 방식 필수 검증
        if (Objects.isNull(data.getGrantTiming())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTiming.required", null, null));
        }

        // 8. 첫 부여 시점 필수 검증 (스케줄러가 반복 부여를 계산하기 위한 기준일)
        if (Objects.isNull(data.getFirstGrantDate())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.firstGrantDate.required", null, null));
        }

        // 9. 부여 시점에 따른 세부 검증
        validateGrantTiming(data);

        // 10. 반복 단위에 따른 세부 검증
        validateRepeatUnit(data);
    }

    /**
     * 부여 시점에 따른 특정 월/일 검증
     * - FIXED_DATE: 특정 월과 일 모두 필수
     * - SPECIFIC_MONTH: 특정 월 필수
     * - SPECIFIC_DAY: 특정 일 필수
     * - QUARTER_END, HALF_END, YEAR_END: 특정 월/일 불필요
     *
     * @param data 휴가 정책 데이터
     */
    private void validateGrantTiming(VacationPolicyServiceDto data) {
        GrantTiming grantTiming = data.getGrantTiming();

        switch (grantTiming) {
            case FIXED_DATE:
                // 고정 날짜: 특정 월과 일 모두 필수
                if (Objects.isNull(data.getSpecificMonths()) || Objects.isNull(data.getSpecificDays())) {
                    throw new IllegalArgumentException(ms.getMessage("vacation.policy.fixedDate.monthDay.required", null, null));
                }
                validateMonth(data.getSpecificMonths());
                validateDay(data.getSpecificDays());
                break;

            case SPECIFIC_MONTH:
                // 특정 월: 월만 필수
                if (Objects.isNull(data.getSpecificMonths())) {
                    throw new IllegalArgumentException(ms.getMessage("vacation.policy.specificMonth.required", null, null));
                }
                validateMonth(data.getSpecificMonths());

                // 특정 일이 설정된 경우도 검증
                if (Objects.nonNull(data.getSpecificDays())) {
                    validateDay(data.getSpecificDays());
                }
                break;

            case SPECIFIC_DAY:
                // 특정 일: 일만 필수
                if (Objects.isNull(data.getSpecificDays())) {
                    throw new IllegalArgumentException(ms.getMessage("vacation.policy.specificDay.required", null, null));
                }
                validateDay(data.getSpecificDays());
                break;

            case QUARTER_END:
            case HALF_END:
            case YEAR_END:
                // 분기말, 반기말, 연말: 특정 월/일 불필요
                if (Objects.nonNull(data.getSpecificMonths()) || Objects.nonNull(data.getSpecificDays())) {
                    throw new IllegalArgumentException(ms.getMessage("vacation.policy.periodEnd.monthDay.unnecessary", null, null));
                }
                break;
        }
    }

    /**
     * 반복 단위에 따른 세부 검증
     * - YEARLY: 연 반복은 특정 월과 일 필요
     * - MONTHLY: 월 반복은 특정 일 필요
     * - DAILY: 일 반복은 특정 월/일 불필요
     *
     * @param data 휴가 정책 데이터
     */
    private void validateRepeatUnit(VacationPolicyServiceDto data) {
        RepeatUnit repeatUnit = data.getRepeatUnit();
        GrantTiming grantTiming = data.getGrantTiming();

        switch (repeatUnit) {
            case YEARLY:
                // 연 반복 + SPECIFIC_MONTH/SPECIFIC_DAY 조합 검증
                if (grantTiming == GrantTiming.SPECIFIC_MONTH && Objects.isNull(data.getSpecificMonths())) {
                    throw new IllegalArgumentException(ms.getMessage("vacation.policy.yearly.specificMonth.required", null, null));
                }
                break;

            case MONTHLY:
                // 월 반복 + SPECIFIC_DAY 조합 검증
                if (grantTiming == GrantTiming.SPECIFIC_DAY && Objects.isNull(data.getSpecificDays())) {
                    throw new IllegalArgumentException(ms.getMessage("vacation.policy.monthly.specificDay.required", null, null));
                }
                break;

            case DAYLY:
                // 일 반복은 특정 월/일이 필요 없음
                break;

            case HALF:
                // 반기 반복
                if (grantTiming == GrantTiming.HALF_END) {
                    // 반기말 부여는 특정 월/일 불필요
                    break;
                }
                break;

            case QUARTERLY:
                // 분기 반복
                if (grantTiming == GrantTiming.QUARTER_END) {
                    // 분기말 부여는 특정 월/일 불필요
                    break;
                }
                break;
        }
    }

    /**
     * 월 유효성 검증 (1~12)
     *
     * @param month 월
     */
    private void validateMonth(Integer month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.month.invalid", null, null));
        }
    }

    /**
     * 일 유효성 검증 (1~31)
     *
     * @param day 일
     */
    private void validateDay(Integer day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.day.invalid", null, null));
        }
    }

    /* ==================== 스케줄러용 날짜 계산 로직 ==================== */

    /**
     * 다음 휴가 부여 예정일 계산<br>
     * 스케줄러에서 사용하며, 정책의 반복 단위와 첫 부여 시점을 기준으로 다음 부여일을 계산
     *
     * @param policy 휴가 정책
     * @param baseDate 기준일 (일반적으로 마지막 부여일 또는 현재 날짜)
     * @return 다음 부여 예정일
     */
    public LocalDate calculateNextGrantDate(VacationPolicy policy, LocalDate baseDate) {
        RepeatUnit repeatUnit = policy.getRepeatUnit();
        Integer repeatInterval = policy.getRepeatInterval();
        LocalDateTime firstGrantDate = policy.getFirstGrantDate();

        // 첫 부여일을 LocalDate로 변환
        LocalDate firstDate = firstGrantDate.toLocalDate();

        // 기준일이 첫 부여일보다 이전이면 첫 부여일 반환
        if (baseDate.isBefore(firstDate)) {
            return firstDate;
        }

        switch (repeatUnit) {
            case YEARLY:
                // 매년 부여: 첫 부여일의 월/일 기준으로 다음 년도 계산
                return calculateNextYearlyDate(firstDate, baseDate, repeatInterval);

            case MONTHLY:
                // 매월 부여: 매월 1일에 부여 (요구사항 기준)
                return calculateNextMonthlyDate(baseDate, repeatInterval);

            case DAYLY:
                // 매일 부여
                return baseDate.plusDays(repeatInterval);

            case QUARTERLY:
                // 분기별 부여: 3개월마다
                return calculateNextQuarterlyDate(firstDate, baseDate);

            case HALF:
                // 반기별 부여: 6개월마다
                return calculateNextHalfYearlyDate(firstDate, baseDate);

            default:
                throw new IllegalArgumentException("지원하지 않는 반복 단위입니다: " + repeatUnit);
        }
    }

    /**
     * 매년 부여의 다음 부여일 계산<br>
     * 첫 부여일의 월/일을 기준으로 매년 동일한 날짜에 부여<br>
     * 예: 첫 부여일이 2024-01-15이면, 2025-01-15, 2026-01-15... 부여
     *
     * @param firstDate 첫 부여일
     * @param baseDate 기준일
     * @param interval 반복 간격 (년)
     * @return 다음 부여일
     */
    private LocalDate calculateNextYearlyDate(LocalDate firstDate, LocalDate baseDate, int interval) {
        int yearsDiff = baseDate.getYear() - firstDate.getYear();
        int nextYearOffset = ((yearsDiff / interval) + 1) * interval;

        LocalDate nextDate = firstDate.plusYears(nextYearOffset);

        // 이미 지난 날짜면 추가로 interval만큼 더함
        if (!nextDate.isAfter(baseDate)) {
            nextDate = nextDate.plusYears(interval);
        }

        return nextDate;
    }

    /**
     * 매월 부여의 다음 부여일 계산<br>
     * 요구사항: 매월 1일에 휴가 부여
     *
     * @param baseDate 기준일
     * @param interval 반복 간격 (월)
     * @return 다음 부여일 (매월 1일)
     */
    private LocalDate calculateNextMonthlyDate(LocalDate baseDate, int interval) {
        // 현재 달의 1일
        LocalDate firstDayOfMonth = baseDate.with(TemporalAdjusters.firstDayOfMonth());

        // 기준일이 1일보다 뒤라면 다음 달 1일
        if (baseDate.isAfter(firstDayOfMonth)) {
            return firstDayOfMonth.plusMonths(interval);
        }

        return firstDayOfMonth;
    }

    /**
     * 분기별 부여의 다음 부여일 계산<br>
     * 1분기: 1월 1일, 2분기: 4월 1일, 3분기: 7월 1일, 4분기: 10월 1일
     *
     * @param firstDate 첫 부여일
     * @param baseDate 기준일
     * @return 다음 부여일
     */
    private LocalDate calculateNextQuarterlyDate(LocalDate firstDate, LocalDate baseDate) {
        int currentYear = baseDate.getYear();
        int currentMonth = baseDate.getMonthValue();

        // 다음 분기 시작월 계산 (1, 4, 7, 10월)
        int nextQuarterStartMonth = ((currentMonth - 1) / 3 + 1) * 3 + 1;

        LocalDate nextDate;
        if (nextQuarterStartMonth > 12) {
            // 내년 1월
            nextDate = LocalDate.of(currentYear + 1, 1, 1);
        } else {
            // 같은 해의 다음 분기
            nextDate = LocalDate.of(currentYear, nextQuarterStartMonth, 1);
        }

        // 이미 지난 날짜면 다음 분기로
        if (!nextDate.isAfter(baseDate)) {
            nextDate = nextDate.plusMonths(3);
        }

        return nextDate;
    }

    /**
     * 반기별 부여의 다음 부여일 계산<br>
     * 상반기: 1월 1일, 하반기: 7월 1일
     *
     * @param firstDate 첫 부여일
     * @param baseDate 기준일
     * @return 다음 부여일
     */
    private LocalDate calculateNextHalfYearlyDate(LocalDate firstDate, LocalDate baseDate) {
        int currentYear = baseDate.getYear();
        int currentMonth = baseDate.getMonthValue();

        LocalDate nextDate;
        if (currentMonth < 7) {
            // 상반기: 7월 1일
            nextDate = LocalDate.of(currentYear, 7, 1);
        } else {
            // 하반기: 내년 1월 1일
            nextDate = LocalDate.of(currentYear + 1, 1, 1);
        }

        // 이미 지난 날짜면 6개월 추가
        if (!nextDate.isAfter(baseDate)) {
            nextDate = nextDate.plusMonths(6);
        }

        return nextDate;
    }

    /**
     * 휴가 만료일 계산<br>
     * 매년 부여: 12월 31일 소멸<br>
     * 매월 부여: 해당 월 말일 소멸
     *
     * @param grantDate 부여일
     * @param repeatUnit 반복 단위
     * @return 만료일
     */
    public LocalDateTime calculateExpiryDate(LocalDate grantDate, RepeatUnit repeatUnit) {
        LocalDate expiryDate;

        switch (repeatUnit) {
            case YEARLY:
                // 매년 부여: 해당 년도 12월 31일 23시 59분 59초에 소멸
                expiryDate = LocalDate.of(grantDate.getYear(), 12, 31);
                return expiryDate.atTime(23, 59, 59);

            case MONTHLY:
                // 매월 부여: 해당 월 말일 23시 59분 59초에 소멸
                expiryDate = grantDate.with(TemporalAdjusters.lastDayOfMonth());
                return expiryDate.atTime(23, 59, 59);

            case DAYLY:
                // 매일 부여: 당일 23시 59분 59초에 소멸
                return grantDate.atTime(23, 59, 59);

            case QUARTERLY:
                // 분기별 부여: 해당 분기 말일 소멸
                int quarterEndMonth = ((grantDate.getMonthValue() - 1) / 3 + 1) * 3;
                expiryDate = LocalDate.of(grantDate.getYear(), quarterEndMonth, 1)
                        .with(TemporalAdjusters.lastDayOfMonth());
                return expiryDate.atTime(23, 59, 59);

            case HALF:
                // 반기별 부여: 해당 반기 말일 소멸 (6월 30일 or 12월 31일)
                int halfEndMonth = grantDate.getMonthValue() <= 6 ? 6 : 12;
                expiryDate = LocalDate.of(grantDate.getYear(), halfEndMonth, 1)
                        .with(TemporalAdjusters.lastDayOfMonth());
                return expiryDate.atTime(23, 59, 59);

            default:
                throw new IllegalArgumentException("지원하지 않는 반복 단위입니다: " + repeatUnit);
        }
    }

    /**
     * 오늘이 휴가 부여일인지 판단<br>
     * 스케줄러에서 부여 대상을 확인할 때 사용
     *
     * @param today 오늘 날짜
     * @param nextGrantDate 다음 부여 예정일
     * @return 부여 대상 여부
     */
    public boolean shouldGrantToday(LocalDate today, LocalDate nextGrantDate) {
        return nextGrantDate != null && !today.isBefore(nextGrantDate);
    }

    /**
     * 오늘이 휴가 소멸일인지 판단<br>
     * 스케줄러에서 소멸 대상을 확인할 때 사용
     *
     * @param today 오늘 날짜
     * @param expiryDate 만료일
     * @return 소멸 대상 여부
     */
    public boolean shouldExpireToday(LocalDate today, LocalDateTime expiryDate) {
        return expiryDate != null && !today.isBefore(expiryDate.toLocalDate());
    }
}
