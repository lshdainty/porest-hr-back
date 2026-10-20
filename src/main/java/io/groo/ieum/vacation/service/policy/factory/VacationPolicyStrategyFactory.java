package io.groo.ieum.vacation.service.policy.factory;

import io.groo.ieum.vacation.repository.VacationPolicyRepository;
import io.groo.ieum.vacation.service.policy.ManualGrant;
import io.groo.ieum.vacation.service.policy.OnRequest;
import io.groo.ieum.vacation.service.policy.RepeatGrant;
import io.groo.ieum.vacation.service.policy.VacationPolicyStrategy;
import io.groo.ieum.vacation.type.GrantMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacationPolicyStrategyFactory {
    private final VacationPolicyRepository vacationPolicyRepository;

    public VacationPolicyStrategy getStrategy(GrantMethod grantMethod) {
        return switch (grantMethod) {
            case ON_REQUEST -> new OnRequest(vacationPolicyRepository);
            case MANUAL_GRANT -> new ManualGrant(vacationPolicyRepository);
            case REPEAT_GRANT -> new RepeatGrant(vacationPolicyRepository);
        };
    }
}