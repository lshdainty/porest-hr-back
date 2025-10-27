package com.lshdainty.porest.vacation.service.policy;

import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.type.GrantTiming;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
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
                data.getSpecificDays()
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

        // 8. 부여 시점에 따른 세부 검증
        validateGrantTiming(data);

        // 9. 반복 단위에 따른 세부 검증
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
}
