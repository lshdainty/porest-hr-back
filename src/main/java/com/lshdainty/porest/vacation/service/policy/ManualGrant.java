package com.lshdainty.porest.vacation.service.policy;

import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class ManualGrant implements VacationPolicyStrategy {
    private final MessageSource ms;
    private final VacationPolicyCustomRepositoryImpl vacationPolicyRepository;

    @Override
    public Long registVacationPolicy(VacationPolicyServiceDto data) {
        // 관리자 직접 부여 방식 검증
        validateManualGrantPolicy(data);

        // 반복 단위, 반복 간격, 부여시점 지정 방식, 특정월, 특정일을 모두 null로 설정하여 강제 저장
        // 관리자가 부여하는 휴가 정책의 경우 스케줄러가 필요없음
        VacationPolicy vacationPolicy = VacationPolicy.createVacationPolicy(
                data.getName(),
                data.getDesc(),
                data.getVacationType(),
                data.getGrantMethod(),
                data.getGrantTime(),
                null,
                null,
                null,
                null,
                null
        );

        vacationPolicyRepository.save(vacationPolicy);
        return vacationPolicy.getId();
    }

    /**
     * 관리자 직접 부여 방식의 휴가 정책 검증
     * 1. 정책명 필수 검증
     * 2. 정책명 중복 검증
     * 3. 스케줄 관련 필드 null 검증
     *
     * 참고: 관리자 직접 부여 방식은 부여 단위를 선택하지 않고 관리자가 직접 수량을 지정
     *
     * @param data 휴가 정책 데이터
     */
    private void validateManualGrantPolicy(VacationPolicyServiceDto data) {
        // 1. 정책명 필수 검증
        if (Objects.isNull(data.getName()) || data.getName().trim().isEmpty()) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.name.required", null, null));
        }

        // 2. 정책명 중복 검증
        if (vacationPolicyRepository.existsByName(data.getName())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.name.duplicate", null, null));
        }

        // 3. 스케줄 관련 필드는 모두 null이어야 함 (관리자가 직접 부여하므로 스케줄러 불필요)
        if (Objects.nonNull(data.getRepeatUnit()) || Objects.nonNull(data.getRepeatInterval()) ||
            Objects.nonNull(data.getGrantTiming()) || Objects.nonNull(data.getSpecificMonths()) ||
            Objects.nonNull(data.getSpecificDays())) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.manual.schedule.unnecessary", null, null));
        }

        // 4. grantTime이 설정된 경우, 0보다 커야 함 (관리자 직접 부여는 grantTime이 optional)
        if (Objects.nonNull(data.getGrantTime()) && data.getGrantTime().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(ms.getMessage("vacation.policy.grantTime.positive", null, null));
        }
    }
}