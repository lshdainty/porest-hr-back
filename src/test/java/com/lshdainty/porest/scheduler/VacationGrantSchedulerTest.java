package com.lshdainty.porest.scheduler;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.UserVacationPolicy;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.UserVacationPolicyRepository;
import com.lshdainty.porest.vacation.repository.VacationGrantRepository;
import com.lshdainty.porest.vacation.scheduler.VacationGrantScheduler;
import com.lshdainty.porest.vacation.service.policy.RepeatGrant;
import com.lshdainty.porest.vacation.service.policy.factory.VacationPolicyStrategyFactory;
import com.lshdainty.porest.vacation.type.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("휴가 부여 스케줄러 테스트")
class VacationGrantSchedulerTest {

    @Mock
    private UserVacationPolicyRepository userVacationPolicyRepository;

    @Mock
    private VacationGrantRepository vacationGrantRepository;

    @Mock
    private VacationPolicyStrategyFactory strategyFactory;

    @Mock
    private RepeatGrant repeatGrant;

    @InjectMocks
    private VacationGrantScheduler scheduler;

    @Nested
    @DisplayName("휴가 만료 처리")
    class ExpireVacationsDaily {
        @Test
        @DisplayName("성공 - 만료 대상 휴가를 만료 처리한다")
        void expireVacationsDailySuccess() {
            // given
            User user = User.createUser("user1");
            VacationPolicy policy = createTestPolicy();
            VacationGrant grant = VacationGrant.createVacationGrant(
                    user, policy, "연차", VacationType.ANNUAL, new BigDecimal("15.0"),
                    LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59)
            );
            ReflectionTestUtils.setField(grant, "id", 1L);

            given(vacationGrantRepository.findExpiredTargets(any())).willReturn(List.of(grant));

            // when
            scheduler.expireVacationsDaily();

            // then
            assertThat(grant.getStatus()).isEqualTo(GrantStatus.EXPIRED);
            then(vacationGrantRepository).should().findExpiredTargets(any());
        }

        @Test
        @DisplayName("성공 - 만료 대상이 없으면 아무 작업도 하지 않는다")
        void expireVacationsDailyEmpty() {
            // given
            given(vacationGrantRepository.findExpiredTargets(any())).willReturn(List.of());

            // when
            scheduler.expireVacationsDaily();

            // then
            then(vacationGrantRepository).should().findExpiredTargets(any());
        }
    }

    @Nested
    @DisplayName("휴가 자동 부여")
    class GrantVacationsDaily {
        @Test
        @DisplayName("성공 - 부여 대상 정책에 휴가를 부여한다")
        void grantVacationsDailySuccess() {
            // given
            User user = User.createUser("user1");
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL,
                    new BigDecimal("15.0"), YNType.N, RepeatUnit.YEARLY, 1, null, null,
                    LocalDateTime.of(2025, 1, 1, 0, 0), YNType.Y, null,
                    EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            ReflectionTestUtils.setField(policy, "id", 1L);

            UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, policy);
            ReflectionTestUtils.setField(uvp, "id", 1L);

            given(strategyFactory.getStrategy(GrantMethod.REPEAT_GRANT)).willReturn(repeatGrant);
            given(userVacationPolicyRepository.findRepeatGrantTargetsForToday(any())).willReturn(List.of(uvp));
            given(repeatGrant.calculateNextGrantDate(any(), any())).willReturn(LocalDate.of(2026, 1, 1));
            willDoNothing().given(vacationGrantRepository).saveAll(anyList());

            // when
            scheduler.grantVacationsDaily();

            // then
            then(userVacationPolicyRepository).should().findRepeatGrantTargetsForToday(any());
            then(vacationGrantRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("성공 - 부여 대상이 없으면 아무 작업도 하지 않는다")
        void grantVacationsDailyEmpty() {
            // given
            given(strategyFactory.getStrategy(GrantMethod.REPEAT_GRANT)).willReturn(repeatGrant);
            given(userVacationPolicyRepository.findRepeatGrantTargetsForToday(any())).willReturn(List.of());

            // when
            scheduler.grantVacationsDaily();

            // then
            then(userVacationPolicyRepository).should().findRepeatGrantTargetsForToday(any());
            then(vacationGrantRepository).shouldHaveNoMoreInteractions();
        }
    }

    private VacationPolicy createTestPolicy() {
        VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                "연차", "연차 정책", VacationType.ANNUAL,
                new BigDecimal("15.0"), YNType.N, YNType.N,
                EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        ReflectionTestUtils.setField(policy, "id", 1L);
        return policy;
    }
}
