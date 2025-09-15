package com.lshdainty.porest.service.vacation.policy;

import com.lshdainty.porest.domain.VacationPolicy;
import com.lshdainty.porest.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.service.VacationService;
import com.lshdainty.porest.service.dto.VacationPolicyServiceDto;

public class OnRequest extends VacationService {
    VacationPolicyCustomRepositoryImpl vacationPolicyRepository;

    public OnRequest(VacationPolicyCustomRepositoryImpl vacationPolicyRepository) {
        super(null, null, null, vacationPolicyRepository, null, null, null);
        this.vacationPolicyRepository = vacationPolicyRepository;
    }

    @Override
    public Long registVacationPolicy(VacationPolicyServiceDto data) {
        // 반복 단위, 반복 간격, 부여시점 지정 방식, 특정월, 특정일을 모두 null로 설정하여 강제 저장
        // 직접 신청하는 휴가의 경우 스케줄러가 필요없음
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
