package com.lshdainty.porest.vacation.service.policy;

import com.lshdainty.porest.common.message.MessageKey;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.common.util.MessageResolver;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationPolicyRepository;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.RepeatUnit;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
public class RepeatGrant implements VacationPolicyStrategy {
    private final MessageResolver messageResolver;
    private final VacationPolicyRepository vacationPolicyRepository;

    @Override
    public Long registVacationPolicy(VacationPolicyServiceDto data) {
        // 반복 휴가 정책의 검증
        validateRepeatGrantPolicy(data);

        VacationPolicy vacationPolicy = VacationPolicy.createRepeatGrantPolicy(
                data.getName(),
                data.getDesc(),
                data.getVacationType(),
                data.getGrantTime(),
                data.getMinuteGrantYn(),
                data.getRepeatUnit(),
                data.getRepeatInterval(),
                data.getSpecificMonths(),
                data.getSpecificDays(),
                data.getFirstGrantDate(),
                data.getIsRecurring(),
                data.getMaxGrantCount(),
                data.getEffectiveType(),
                data.getExpirationType()
        );

        vacationPolicyRepository.save(vacationPolicy);
        return vacationPolicy.getId();
    }

    /**
     * 반복 부여 방식의 휴가 정책 검증
     * 1. 정책명 필수 검증
     * 2. 정책명 중복 검증
     * 3. 부여시간 필수 및 양수 검증
     * 4. minuteGrantYn 필수 검증
     * 5. 반복 단위 필수 검증
     * 6. 반복 간격 필수 및 양수 검증
     * 7. 첫 부여 시점 필수 검증
     * 8. 반복 단위에 따른 specificMonths/Days 검증
     * 9. 1회성 부여 관련 필드 검증
     *
     * @param data 휴가 정책 데이터
     */
    private void validateRepeatGrantPolicy(VacationPolicyServiceDto data) {
        // 1. 정책명 필수 검증
        if (Objects.isNull(data.getName()) || data.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_NAME_REQUIRED));
        }

        // 2. 정책명 중복 검증
        if (vacationPolicyRepository.existsByName(data.getName())) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_NAME_DUPLICATE));
        }

        // 3. 부여시간 필수 검증 (스케줄러에서 휴가 부여를 위해 필수)
        if (Objects.isNull(data.getGrantTime())) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_GRANT_TIME_REQUIRED));
        }

        // 4. 부여시간은 0보다 커야 함
        if (data.getGrantTime().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_GRANT_TIME_POSITIVE));
        }

        // 5. minuteGrantYn 필수 검증
        if (Objects.isNull(data.getMinuteGrantYn())) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_MINUTE_GRANT_YN_REQUIRED));
        }

        // 6. 반복 단위 필수 검증
        if (Objects.isNull(data.getRepeatUnit())) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_REPEAT_UNIT_REQUIRED));
        }

        // 7. 반복 간격 필수 및 양수 검증
        if (Objects.isNull(data.getRepeatInterval()) || data.getRepeatInterval() <= 0) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_REPEAT_INTERVAL_POSITIVE));
        }

        // 8. 첫 부여 시점 필수 검증 (스케줄러가 반복 부여를 계산하기 위한 기준일)
        if (Objects.isNull(data.getFirstGrantDate())) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_FIRST_GRANT_DATE_REQUIRED));
        }

        // 9. 반복 단위에 따른 specificMonths/Days 검증
        validateRepeatUnit(data);

        // 10. 1회성 부여 관련 필드 검증
        validateOneTimeGrant(data);

        // 11. effectiveType 필수 검증
        if (Objects.isNull(data.getEffectiveType())) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_EFFECTIVE_TYPE_REQUIRED));
        }

        // 12. expirationType 필수 검증
        if (Objects.isNull(data.getExpirationType())) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_EXPIRATION_TYPE_REQUIRED));
        }
    }


    /**
     * 반복 단위에 따른 세부 검증
     * - YEARLY: 매년 반복 (specificMonths/Days 조합 검증)
     * - MONTHLY: 매월 반복 (specificMonths 사용 불가)
     * - QUARTERLY: 분기 반복 (specificMonths 사용 불가)
     * - HALF: 반기 반복 (specificMonths 사용 불가)
     * - DAILY: 매일 반복 (specificMonths/Days 둘 다 사용 불가)
     *
     * @param data 휴가 정책 데이터
     */
    private void validateRepeatUnit(VacationPolicyServiceDto data) {
        RepeatUnit repeatUnit = data.getRepeatUnit();
        Integer months = data.getSpecificMonths();
        Integer days = data.getSpecificDays();

        switch (repeatUnit) {
            case YEARLY:
                // YEARLY는 4가지 패턴만 허용:
                // 1) months=null, days=null: firstGrantDate 기준 매년
                // 2) months=X, days=null: 매년 X월 1일
                // 3) months=X, days=Y: 매년 X월 Y일 (Y가 해당 월의 일수를 초과하면 마지막 날로 조정)
                // 4) months=null, days=Y: 허용 안 함 (어느 달인지 모름)
                if (months == null && days != null) {
                    throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_YEARLY_MONTH_REQUIRED));
                }
                if (months != null) {
                    validateMonth(months);
                    if (days != null) {
                        validateDay(days);
                        // 월별 최대 일수 검증은 제거 - 스케줄러에서 해당 월의 마지막 날로 조정
                    }
                }
                break;

            case MONTHLY:
                // MONTHLY는 2가지 패턴만 허용:
                // 1) months=null, days=null: 매월 1일
                // 2) months=null, days=X: 매월 X일
                // 3) months=X, days=any: 허용 안 함 (매월인데 특정 월?)
                if (months != null) {
                    throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_MONTHLY_MONTH_NOT_ALLOWED));
                }
                if (days != null) {
                    validateDay(days);
                }
                break;

            case QUARTERLY:
                // QUARTERLY: months 지정 불가, days는 선택
                // days 지정 시 각 분기 시작월(1,4,7,10월)의 X일에 부여
                // 해당 월에 X일이 없으면 해당 월의 마지막 날로 조정 (스케줄러에서 처리)
                if (months != null) {
                    throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_QUARTERLY_MONTH_NOT_ALLOWED));
                }
                if (days != null) {
                    validateDay(days);
                }
                break;

            case HALF:
                // HALF: months 지정 불가, days는 선택
                // days 지정 시 각 반기 시작월(1,7월)의 X일에 부여
                // 해당 월에 X일이 없으면 해당 월의 마지막 날로 조정 (스케줄러에서 처리)
                if (months != null) {
                    throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_HALF_MONTH_NOT_ALLOWED));
                }
                if (days != null) {
                    validateDay(days);
                }
                break;

            case DAILY:
                // DAILY: months, days 둘 다 불가
                if (months != null || days != null) {
                    throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_DAILY_MONTH_DAY_NOT_ALLOWED));
                }
                break;
        }

        // repeatInterval 최댓값 검증 (비현실적인 큰 값 방지)
        if (data.getRepeatInterval() > 100) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_REPEAT_INTERVAL_TOO_LARGE));
        }
    }

    /**
     * 월 유효성 검증 (1~12)
     *
     * @param month 월
     */
    private void validateMonth(Integer month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_MONTH_INVALID));
        }
    }

    /**
     * 일 유효성 검증 (1~31)
     *
     * @param day 일
     */
    private void validateDay(Integer day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_DAY_INVALID));
        }
    }

    /**
     * 특정 월의 특정 일을 해당 월의 실제 마지막 날로 조정
     * 예: 2월 31일 → 2월 28일(또는 29일), 4월 31일 → 4월 30일
     *
     * @param year 년도
     * @param month 월 (1~12)
     * @param day 일 (1~31)
     * @return 조정된 LocalDate
     */
    private LocalDate adjustToValidDate(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, 1);
        int lastDayOfMonth = date.lengthOfMonth();

        // day가 해당 월의 마지막 날보다 크면 마지막 날로 조정
        int adjustedDay = Math.min(day, lastDayOfMonth);

        return LocalDate.of(year, month, adjustedDay);
    }

    /**
     * 1회성 부여 관련 필드 검증
     * - isRecurring이 N인 경우 maxGrantCount는 필수
     * - isRecurring이 Y인 경우 maxGrantCount는 null이어야 함
     * - maxGrantCount가 설정된 경우 양수여야 함
     *
     * @param data 휴가 정책 데이터
     */
    private void validateOneTimeGrant(VacationPolicyServiceDto data) {
        YNType isRecurring = data.getIsRecurring();
        Integer maxGrantCount = data.getMaxGrantCount();

        // isRecurring이 null인 경우 기본값은 Y (반복 부여)
        if (Objects.isNull(isRecurring)) {
            return;
        }

        // 1회성 부여(isRecurring=N)인 경우 maxGrantCount 필수
        if (YNType.isN(isRecurring)) {
            if (Objects.isNull(maxGrantCount)) {
                throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_MAX_GRANT_COUNT_REQUIRED));
            }
            if (maxGrantCount <= 0) {
                throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_MAX_GRANT_COUNT_POSITIVE));
            }
        }

        // 반복 부여(isRecurring=Y)인 경우 maxGrantCount는 null이어야 함
        if (YNType.isY(isRecurring) && Objects.nonNull(maxGrantCount)) {
            throw new IllegalArgumentException(messageResolver.getMessage(MessageKey.VACATION_POLICY_MAX_GRANT_COUNT_UNNECESSARY));
        }
    }

    /* ==================== 스케줄러용 날짜 계산 로직 ==================== */

    /**
     * 다음 휴가 부여 예정일 계산<br>
     * 스케줄러에서 사용하며, 정책의 반복 단위와 첫 부여 시점을 기준으로 다음 부여일을 계산<br>
     * 1회성 부여 정책의 경우, 이미 부여되었다면 null을 반환하여 재부여를 방지
     *
     * @param policy 휴가 정책
     * @param baseDate 기준일 (일반적으로 마지막 부여일 또는 현재 날짜)
     * @return 다음 부여 예정일 (1회성 정책에서 이미 부여되었다면 null)
     */
    public LocalDate calculateNextGrantDate(VacationPolicy policy, LocalDate baseDate) {
        RepeatUnit repeatUnit = policy.getRepeatUnit();
        Integer repeatInterval = policy.getRepeatInterval();
        LocalDateTime firstGrantDate = policy.getFirstGrantDate();
        YNType isRecurring = policy.getIsRecurring();

        // 첫 부여일을 LocalDate로 변환
        LocalDate firstDate = firstGrantDate.toLocalDate();

        // 기준일이 첫 부여일보다 이전이면 첫 부여일 반환
        if (baseDate.isBefore(firstDate)) {
            return firstDate;
        }

        // 1회성 부여 정책 처리: 이미 부여일이 지났다면 null 반환 (재부여 방지)
        if (YNType.isN(isRecurring)) {
            // 첫 부여일이 지났다면 더 이상 부여하지 않음
            if (!baseDate.isBefore(firstDate)) {
                return null;
            }
            return firstDate;
        }

        switch (repeatUnit) {
            case YEARLY:
                // 매년 부여: 첫 부여일의 월/일 기준으로 다음 년도 계산
                return calculateNextYearlyDate(firstDate, baseDate, repeatInterval,
                    policy.getSpecificMonths(), policy.getSpecificDays());

            case MONTHLY:
                // 매월 부여: 매월 1일에 부여 (요구사항 기준)
                return calculateNextMonthlyDate(baseDate, repeatInterval, policy.getSpecificDays());

            case DAILY:
                // 매일 부여
                return baseDate.plusDays(repeatInterval);

            case QUARTERLY:
                // 분기별 부여: 3개월마다
                return calculateNextQuarterlyDate(baseDate, policy.getSpecificDays());

            case HALF:
                // 반기별 부여: 6개월마다
                return calculateNextHalfYearlyDate(baseDate, policy.getSpecificDays());

            default:
                throw new IllegalArgumentException("지원하지 않는 반복 단위입니다: " + repeatUnit);
        }
    }

    /**
     * 매년 부여의 다음 부여일 계산<br>
     * specificMonths/Days 지정 시 해당 월/일에 부여<br>
     * 해당 월에 지정된 일이 없으면 해당 월의 마지막 날로 조정<br>
     * 예: months=2, days=31 → 2월 28일(또는 29일)
     *
     * @param firstDate 첫 부여일
     * @param baseDate 기준일
     * @param interval 반복 간격 (년)
     * @param specificMonths 특정 월 (null이면 firstDate의 월 사용)
     * @param specificDays 특정 일 (null이면 firstDate의 일 사용)
     * @return 다음 부여일
     */
    private LocalDate calculateNextYearlyDate(LocalDate firstDate, LocalDate baseDate, int interval,
                                               Integer specificMonths, Integer specificDays) {
        int yearsDiff = baseDate.getYear() - firstDate.getYear();
        int nextYearOffset = ((yearsDiff / interval) + 1) * interval;
        int nextYear = firstDate.getYear() + nextYearOffset;

        // 월/일 결정
        int targetMonth = specificMonths != null ? specificMonths : firstDate.getMonthValue();
        int targetDay = specificDays != null ? specificDays : firstDate.getDayOfMonth();

        // 해당 월의 유효한 날짜로 조정
        LocalDate nextDate = adjustToValidDate(nextYear, targetMonth, targetDay);

        // 이미 지난 날짜면 추가로 interval만큼 더함
        if (!nextDate.isAfter(baseDate)) {
            nextDate = adjustToValidDate(nextYear + interval, targetMonth, targetDay);
        }

        return nextDate;
    }

    /**
     * 매월 부여의 다음 부여일 계산<br>
     * specificDays 지정 시 매월 해당 일에 부여 (없으면 1일)<br>
     * 해당 월에 지정된 일이 없으면 해당 월의 마지막 날로 조정<br>
     * 예: days=31이면 1월31일, 2월28일(or29일), 3월31일...
     *
     * @param baseDate 기준일
     * @param interval 반복 간격 (월)
     * @param specificDays 특정 일 (null이면 1일)
     * @return 다음 부여일
     */
    private LocalDate calculateNextMonthlyDate(LocalDate baseDate, int interval, Integer specificDays) {
        int targetDay = specificDays != null ? specificDays : 1;

        // 현재 달의 targetDay
        LocalDate targetDateThisMonth = adjustToValidDate(baseDate.getYear(), baseDate.getMonthValue(), targetDay);

        // 기준일이 이번 달 targetDay보다 뒤라면 다음 달
        if (baseDate.isAfter(targetDateThisMonth) || baseDate.equals(targetDateThisMonth)) {
            LocalDate nextMonth = baseDate.plusMonths(interval);
            return adjustToValidDate(nextMonth.getYear(), nextMonth.getMonthValue(), targetDay);
        }

        return targetDateThisMonth;
    }

    /**
     * 분기별 부여의 다음 부여일 계산<br>
     * specificDays 지정 시 각 분기 시작월(1,4,7,10월)의 해당 일에 부여<br>
     * 해당 월에 지정된 일이 없으면 해당 월의 마지막 날로 조정<br>
     * 예: days=31이면 1/31, 4/30, 7/31, 10/31
     *
     * @param baseDate 기준일
     * @param specificDays 특정 일 (null이면 1일)
     * @return 다음 부여일
     */
    private LocalDate calculateNextQuarterlyDate(LocalDate baseDate, Integer specificDays) {
        int currentYear = baseDate.getYear();
        int currentMonth = baseDate.getMonthValue();
        int targetDay = specificDays != null ? specificDays : 1;

        // 다음 분기 시작월 계산 (1, 4, 7, 10월)
        int nextQuarterStartMonth = ((currentMonth - 1) / 3 + 1) * 3 + 1;

        LocalDate nextDate;
        if (nextQuarterStartMonth > 12) {
            // 내년 1월
            nextDate = adjustToValidDate(currentYear + 1, 1, targetDay);
        } else {
            // 같은 해의 다음 분기
            nextDate = adjustToValidDate(currentYear, nextQuarterStartMonth, targetDay);
        }

        // 이미 지난 날짜면 다음 분기로
        if (!nextDate.isAfter(baseDate)) {
            LocalDate temp = nextDate.plusMonths(3);
            nextDate = adjustToValidDate(temp.getYear(), temp.getMonthValue(), targetDay);
        }

        return nextDate;
    }

    /**
     * 반기별 부여의 다음 부여일 계산<br>
     * specificDays 지정 시 각 반기 시작월(1,7월)의 해당 일에 부여<br>
     * 해당 월에 지정된 일이 없으면 해당 월의 마지막 날로 조정<br>
     * 예: days=31이면 1/31, 7/31
     *
     * @param baseDate 기준일
     * @param specificDays 특정 일 (null이면 1일)
     * @return 다음 부여일
     */
    private LocalDate calculateNextHalfYearlyDate(LocalDate baseDate, Integer specificDays) {
        int currentYear = baseDate.getYear();
        int currentMonth = baseDate.getMonthValue();
        int targetDay = specificDays != null ? specificDays : 1;

        LocalDate nextDate;
        if (currentMonth < 7) {
            // 상반기: 7월
            nextDate = adjustToValidDate(currentYear, 7, targetDay);
        } else {
            // 하반기: 내년 1월
            nextDate = adjustToValidDate(currentYear + 1, 1, targetDay);
        }

        // 이미 지난 날짜면 6개월 추가
        if (!nextDate.isAfter(baseDate)) {
            LocalDate temp = nextDate.plusMonths(6);
            nextDate = adjustToValidDate(temp.getYear(), temp.getMonthValue(), targetDay);
        }

        return nextDate;
    }

    /* ==================== 한국어 설명 생성 로직 ==================== */

    /**
     * 반복 부여 정책을 한국어로 설명하는 문자열 생성<br>
     * 예시:<br>
     * - "매년 1월 1일 부여"<br>
     * - "2년 간격으로 매년 1월 1일 부여"<br>
     * - "매년 1월 1일, 7년 후 1회 부여"<br>
     * - "매월 15일 부여"<br>
     * - "분기마다 1일 부여"<br>
     * - "반기마다 부여"<br>
     *
     * @param policy 휴가 정책
     * @return 한국어 설명 문자열
     */
    public static String generateRepeatGrantDescription(VacationPolicy policy) {
        if (policy == null || policy.getGrantMethod() != GrantMethod.REPEAT_GRANT) {
            return null;
        }

        RepeatUnit repeatUnit = policy.getRepeatUnit();
        Integer repeatInterval = policy.getRepeatInterval();
        Integer specificMonths = policy.getSpecificMonths();
        Integer specificDays = policy.getSpecificDays();
        YNType isRecurring = policy.getIsRecurring();
        Integer maxGrantCount = policy.getMaxGrantCount();

        StringBuilder description = new StringBuilder();

        // 1. 반복 간격 (2년 이상일 경우만 표시)
        if (repeatInterval != null && repeatInterval > 1) {
            description.append(repeatInterval).append(getRepeatUnitSuffix(repeatUnit)).append(" 간격으로 ");
        }

        // 2. 반복 단위 (매년, 매월, 매일, 분기, 반기)
        if (repeatUnit != null) {
            description.append(repeatUnit.getViewName());
        }

        // 3. 특정 월/일 (반복 단위에 따라)
        switch (repeatUnit) {
            case YEARLY:
                // 매년 X월 Y일 형태
                if (specificMonths != null) {
                    description.append(" ").append(specificMonths).append("월");
                }
                if (specificDays != null) {
                    description.append(" ").append(specificDays).append("일");
                }
                break;

            case MONTHLY:
                // 매월 Y일 형태
                if (specificDays != null) {
                    description.append(" ").append(specificDays).append("일");
                }
                break;

            case QUARTERLY:
            case HALF:
                // 분기/반기마다 Y일 형태
                if (specificDays != null) {
                    description.append(" ").append(specificDays).append("일");
                }
                break;

            case DAILY:
                // 매일은 추가 정보 없음
                break;
        }

        // 4. "부여" 추가
        description.append(" 부여");

        // 5. 1회성 부여 여부
        if (YNType.isN(isRecurring) && maxGrantCount != null) {
            // 첫 부여일 기준으로 몇 년 후인지 계산
            LocalDateTime firstGrantDate = policy.getFirstGrantDate();
            if (firstGrantDate != null && repeatUnit == RepeatUnit.YEARLY && repeatInterval != null) {
                int yearsLater = repeatInterval * (maxGrantCount - 1);
                if (yearsLater > 0) {
                    description.append(", ").append(yearsLater).append("년 후");
                }
            }
            description.append(" ").append(maxGrantCount).append("회 부여");
        }

        return description.toString();
    }

    /**
     * 반복 단위에 따른 접미사 반환 (간격 표시용)<br>
     * 예: YEARLY → "년", MONTHLY → "개월", DAILY → "일"
     *
     * @param repeatUnit 반복 단위
     * @return 접미사 문자열
     */
    private static String getRepeatUnitSuffix(RepeatUnit repeatUnit) {
        switch (repeatUnit) {
            case YEARLY:
                return "년";
            case MONTHLY:
                return "개월";
            case DAILY:
                return "일";
            case QUARTERLY:
                return "분기";
            case HALF:
                return "반기";
            default:
                return "";
        }
    }
}
