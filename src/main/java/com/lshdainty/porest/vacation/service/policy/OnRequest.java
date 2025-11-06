package com.lshdainty.porest.vacation.service.policy;

import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.vacation.type.VacationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor
public class OnRequest implements VacationPolicyStrategy {
    private final MessageSource ms;
    private final VacationPolicyCustomRepositoryImpl vacationPolicyRepository;

    @Override
    public Long registVacationPolicy(VacationPolicyServiceDto data) {
        // 신청시 부여 방식 검증
        validateOnRequestPolicy(data);

        // 반복 단위, 반복 간격, 부여시점 지정 방식, 특정월, 특정일, 첫 부여 시점을 모두 null로 설정하여 강제 저장
        // 직접 신청하는 휴가의 경우 스케줄러가 필요없음
        VacationPolicy vacationPolicy = VacationPolicy.createOnRequestPolicy(
                data.getName(),
                data.getDesc(),
                data.getVacationType(),
                data.getGrantTime(),
                data.getGrantTimeExists(),
                data.getMinuteGrantYn(),
                data.getApprovalRequiredCount(),
                data.getEffectiveType(),   // effectiveType
                data.getExpirationType()   // expirationType
        );

        vacationPolicyRepository.save(vacationPolicy);
        return vacationPolicy.getId();
    }

    /**
     * 신청시 부여 방식의 휴가 정책 검증
     * 1. 정책명 필수 검증
     * 2. 부여시간 필수 및 양수 검증
     * 3. 정책명 중복 검증
     *
     * @param data 휴가 정책 데이터
     */
    private void validateOnRequestPolicy(VacationPolicyServiceDto data) {
        // 1. 정책명 필수 검증
        if (Objects.isNull(data.getName()) || data.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.name.required", null, null));
        }

        // 2. 부여시간 필수 검증 (신청시 부여할 수량 지정 필요)
        if (Objects.isNull(data.getGrantTime())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTime.required", null, null));
        }

        // 3. 부여시간은 0보다 커야 함
        if (data.getGrantTime().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTime.positive", null, null));
        }

        // 4. 정책명 중복 검증
        if (vacationPolicyRepository.existsByName(data.getName())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.name.duplicate", null, null));
        }

        // 5. effectiveType 필수 검증
        if (Objects.isNull(data.getEffectiveType())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.effectiveType.required", null, null));
        }

        // 6. expirationType 필수 검증
        if (Objects.isNull(data.getExpirationType())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.expirationType.required", null, null));
        }
    }

    /**
     * ON_REQUEST 방식의 부여 시간 계산
     *
     * @param policy 휴가 정책
     * @param requestStartTime 신청 시작 일시
     * @param requestEndTime 신청 종료 일시
     * @return 계산된 부여 시간
     */
    public BigDecimal calculateGrantTime(VacationPolicy policy, LocalDateTime requestStartTime, LocalDateTime requestEndTime) {
        VacationType vacationType = policy.getVacationType();

        // grantTimeExists가 Y인 경우: 정책에 정의된 시간 사용
        if (policy.getGrantTimeExists() == com.lshdainty.porest.common.type.YNType.Y) {
            BigDecimal policyGrantTime = policy.getGrantTime();
            if (policyGrantTime == null) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.grantTimeNotDefined", null, null)
                );
            }
            return policyGrantTime;
        }

        // grantTimeExists가 N인 경우: 동적 계산 (OVERTIME 등)
        // OVERTIME 타입인 경우: 시작/종료 시간 차이를 계산
        if (vacationType == VacationType.OVERTIME) {
            // 필수 값 검증
            if (requestStartTime == null) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.startTimeRequired", null, null)
                );
            }
            if (requestEndTime == null) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.endTimeRequired", null, null)
                );
            }

            // 종료 시간이 시작 시간보다 이후인지 검증
            if (!requestEndTime.isAfter(requestStartTime)) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.endTimeAfterStartTime", null, null)
                );
            }

            // 시간 차이 계산 (분 단위)
            long minutes = Duration.between(requestStartTime, requestEndTime).toMinutes();

            // minuteGrantYn에 따라 분단위 부여 여부 결정
            if (policy.getMinuteGrantYn() == com.lshdainty.porest.common.type.YNType.Y) {
                // 분단위 부여: 30분 단위로 계산
                // 예: 90분 → 1.5시간 → 0.1875 (1시간 + 30분)
                long hours = minutes / 60;
                long remainingMinutes = minutes % 60;

                // 30분 단위로 반올림
                BigDecimal halfHours = remainingMinutes >= 30 ? BigDecimal.valueOf(0.5) : BigDecimal.ZERO;

                return BigDecimal.valueOf(hours)
                        .add(halfHours)
                        .multiply(BigDecimal.valueOf(0.1250))
                        .setScale(4, RoundingMode.DOWN);
            } else {
                // 시간 단위만 부여: 소수점 버림
                // 예: 90분 → 1시간 → 0.1250
                long hours = minutes / 60;

                return BigDecimal.valueOf(hours)
                        .multiply(BigDecimal.valueOf(0.1250))
                        .setScale(4, RoundingMode.DOWN);
            }
        }

        // 그 외의 경우는 에러
        throw new IllegalArgumentException(
                ms.getMessage("error.validate.vacation.cannotCalculateGrantTime", null, null)
        );
    }
}
