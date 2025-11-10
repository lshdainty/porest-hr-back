package com.lshdainty.porest.vacation.service.policy;

import com.lshdainty.porest.common.type.YNType;
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
                data.getIsFlexibleGrant(),
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
     * 2. 정책명 중복 검증
     * 3. isFlexibleGrant에 따른 grantTime 검증
     * 4. minuteGrantYn 필수 검증
     *
     * @param data 휴가 정책 데이터
     */
    private void validateOnRequestPolicy(VacationPolicyServiceDto data) {
        // 1. 정책명 필수 검증
        if (Objects.isNull(data.getName()) || data.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.name.required", null, null));
        }

        // 2. 정책명 중복 검증
        if (vacationPolicyRepository.existsByName(data.getName())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.name.duplicate", null, null));
        }

        // 3. isFlexibleGrant 필수 검증
        if (Objects.isNull(data.getIsFlexibleGrant())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.isFlexibleGrant.required", null, null));
        }

        // 4. isFlexibleGrant에 따른 grantTime 검증
        if (YNType.isY(data.getIsFlexibleGrant())) {
            // isFlexibleGrant가 Y인 경우: grantTime은 null이어야 함 (가변 부여, 동적 계산)
            if (Objects.nonNull(data.getGrantTime())) {
                throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTime.unnecessary", null, null));
            }
        } else {
            // isFlexibleGrant가 N인 경우: grantTime 필수 및 양수 검증 (고정 부여)
            if (Objects.isNull(data.getGrantTime())) {
                throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTime.required", null, null));
            }
            if (data.getGrantTime().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTime.positive", null, null));
            }
        }

        // 5. minuteGrantYn 필수 검증
        if (Objects.isNull(data.getMinuteGrantYn())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.minuteGrantYn.required", null, null));
        }

        // 6. effectiveType 필수 검증
        if (Objects.isNull(data.getEffectiveType())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.effectiveType.required", null, null));
        }

        // 7. expirationType 필수 검증
        if (Objects.isNull(data.getExpirationType())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.expirationType.required", null, null));
        }
    }

    /**
     * ON_REQUEST 방식의 부여 시간 계산
     *
     * @param policy 휴가 정책
     * @param userGrantTime 사용자가 입력한 부여 시간 (isFlexibleGrant=Y일 경우)
     * @return 계산된 부여 시간
     */
    public BigDecimal calculateGrantTime(VacationPolicy policy, BigDecimal userGrantTime) {
        // isFlexibleGrant가 N인 경우: 정책에 정의된 시간 사용 (고정 부여)
        if (YNType.isN(policy.getIsFlexibleGrant())) {
            BigDecimal policyGrantTime = policy.getGrantTime();
            if (Objects.isNull(policyGrantTime)) {
                throw new IllegalArgumentException(
                        ms.getMessage("error.validate.vacation.grantTimeNotDefined", null, null)
                );
            }
            return policyGrantTime;
        }

        // isFlexibleGrant가 Y인 경우: 사용자가 입력한 시간 사용 (가변 부여)
        if (Objects.isNull(userGrantTime)) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.userGrantTimeRequired", null, null)
            );
        }

        // 사용자 입력값 양수 검증
        if (userGrantTime.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    ms.getMessage("error.validate.vacation.userGrantTimePositive", null, null)
            );
        }

        return userGrantTime;
    }
}
