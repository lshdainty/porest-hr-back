package com.lshdainty.porest.vacation.service.policy;

import com.lshdainty.porest.vacation.service.dto.VacationPolicyServiceDto;

public interface VacationPolicyStrategy {
    Long registVacationPolicy(VacationPolicyServiceDto data);
}
