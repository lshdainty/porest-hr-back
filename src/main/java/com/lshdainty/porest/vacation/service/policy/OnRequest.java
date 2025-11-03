package com.lshdainty.porest.vacation.service.policy;

import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
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
}
