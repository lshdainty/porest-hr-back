package io.groo.ieum.service.policy;

import io.groo.ieum.vacation.repository.VacationPolicyRepository;
import io.groo.ieum.vacation.service.policy.ManualGrant;
import io.groo.ieum.vacation.service.policy.OnRequest;
import io.groo.ieum.vacation.service.policy.RepeatGrant;
import io.groo.ieum.vacation.service.policy.VacationPolicyStrategy;
import io.groo.ieum.vacation.service.policy.factory.VacationPolicyStrategyFactory;
import io.groo.ieum.vacation.type.GrantMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("휴가 정책 전략 팩토리 테스트")
class VacationPolicyStrategyFactoryTest {

    @Mock
    private MessageSource ms;

    @Mock
    private VacationPolicyRepository vacationPolicyRepository;

    @InjectMocks
    private VacationPolicyStrategyFactory factory;

    @Test
    @DisplayName("ON_REQUEST 전략을 반환한다")
    void getOnRequestStrategy() {
        // when
        VacationPolicyStrategy strategy = factory.getStrategy(GrantMethod.ON_REQUEST);

        // then
        assertThat(strategy).isInstanceOf(OnRequest.class);
    }

    @Test
    @DisplayName("MANUAL_GRANT 전략을 반환한다")
    void getManualGrantStrategy() {
        // when
        VacationPolicyStrategy strategy = factory.getStrategy(GrantMethod.MANUAL_GRANT);

        // then
        assertThat(strategy).isInstanceOf(ManualGrant.class);
    }

    @Test
    @DisplayName("REPEAT_GRANT 전략을 반환한다")
    void getRepeatGrantStrategy() {
        // when
        VacationPolicyStrategy strategy = factory.getStrategy(GrantMethod.REPEAT_GRANT);

        // then
        assertThat(strategy).isInstanceOf(RepeatGrant.class);
    }
}
