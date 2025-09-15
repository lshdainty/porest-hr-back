package com.lshdainty.porest.service.vacation.policy;

import com.lshdainty.porest.domain.VacationPolicy;
import com.lshdainty.porest.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.service.VacationService;
import com.lshdainty.porest.service.dto.VacationPolicyServiceDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManualGrant extends VacationService {
    VacationPolicyCustomRepositoryImpl vacationPolicyRepository;

    public ManualGrant(VacationPolicyCustomRepositoryImpl vacationPolicyRepository) {
        super(null, null, null, vacationPolicyRepository, null, null, null);
        this.vacationPolicyRepository = vacationPolicyRepository;
    }

    @Override
    public Long registVacationPolicy(VacationPolicyServiceDto data) {
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
}