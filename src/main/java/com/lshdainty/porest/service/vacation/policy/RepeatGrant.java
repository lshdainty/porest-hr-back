package com.lshdainty.porest.service.vacation.policy;

import com.lshdainty.porest.domain.VacationPolicy;
import com.lshdainty.porest.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.service.VacationService;
import com.lshdainty.porest.service.dto.VacationPolicyServiceDto;
import com.lshdainty.porest.type.vacation.RepeatUnit;
import org.springframework.context.MessageSource;

import java.util.Objects;

public class RepeatGrant extends VacationService {
    MessageSource ms;
    VacationPolicyCustomRepositoryImpl vacationPolicyRepository;

    public RepeatGrant(MessageSource ms, VacationPolicyCustomRepositoryImpl vacationPolicyRepository) {
        super(ms, null, null, vacationPolicyRepository, null, null, null);
        this.ms = ms;
        this.vacationPolicyRepository = vacationPolicyRepository;
    }

    @Override
    public Long registVacationPolicy(VacationPolicyServiceDto data) {
        // 반복 휴가 정책의 경우 반복에 대한 값이 정확하게 설정되어야 한다.
        // 부여 시간이 null인 경우 에러 반환(스케줄러에서 휴가 부여 불가능
        if (Objects.isNull(data.getGrantTime())) {
            throw new IllegalArgumentException(ms.getMessage("error.validate.vacation.policy.granttime", null, null));
        }

        // 반복 단위가 월, 반기, 분기인 경우 반복 간격이 1월부터 12월 사이에 있는지 확인
        if (data.getRepeatUnit().equals(RepeatUnit.MONTHLY) ||
            data.getRepeatUnit().equals(RepeatUnit.HALF) ||
            data.getRepeatUnit().equals(RepeatUnit.QUARTERLY)) {
            if (0 > data.getRepeatInterval() || data.getRepeatInterval() > 12) {
                throw new IllegalArgumentException(ms.getMessage("error.validate.vacation.policy.repeatinterval", null, null));
            }
        }



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
}
