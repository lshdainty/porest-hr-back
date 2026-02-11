package com.porest.hr.vacation.service.policy;

import com.porest.hr.vacation.service.dto.VacationPolicyServiceDto;

public interface VacationPolicyStrategy {
    Long registVacationPolicy(VacationPolicyServiceDto data);
}
