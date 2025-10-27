package com.lshdainty.porest.vacation.service.policy.factory;

import com.lshdainty.porest.vacation.repository.VacationPolicyCustomRepositoryImpl;
import com.lshdainty.porest.vacation.service.policy.ManualGrant;
import com.lshdainty.porest.vacation.service.policy.OnRequest;
import com.lshdainty.porest.vacation.service.policy.RepeatGrant;
import com.lshdainty.porest.vacation.service.policy.VacationPolicyStrategy;
import com.lshdainty.porest.vacation.type.GrantMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VacationPolicyStrategyFactory {
    private final MessageSource ms;
    private final VacationPolicyCustomRepositoryImpl vacationPolicyRepository;

    public VacationPolicyStrategy getStrategy(GrantMethod grantMethod) {
        return switch (grantMethod) {
            case ON_REQUEST -> new OnRequest(ms, vacationPolicyRepository);
            case MANUAL_GRANT -> new ManualGrant(ms, vacationPolicyRepository);
            case REPEAT_GRANT -> new RepeatGrant(ms, vacationPolicyRepository);
        };
    }
}