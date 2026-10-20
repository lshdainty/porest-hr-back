package io.groo.ieum.vacation.service.policy;

import io.groo.ieum.vacation.service.dto.VacationPolicyServiceDto;

public interface VacationPolicyStrategy {
    Long registVacationPolicy(VacationPolicyServiceDto data);
}
