package com.porest.hr.scheduler;

import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.user.domain.User;
import com.porest.hr.vacation.domain.VacationGrant;
import com.porest.hr.vacation.domain.VacationGrantSchedule;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.VacationGrantRepository;
import com.porest.hr.vacation.repository.VacationGrantScheduleRepository;
import com.porest.hr.vacation.scheduler.VacationGrantScheduler;
import com.porest.hr.vacation.service.policy.RepeatGrant;
import com.porest.hr.vacation.service.policy.factory.VacationPolicyStrategyFactory;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
import com.porest.hr.vacation.type.GrantMethod;
import com.porest.hr.vacation.type.GrantStatus;
import com.porest.hr.vacation.type.RepeatUnit;
import com.porest.hr.vacation.type.VacationType;
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
    private VacationGrantScheduleRepository vacationGrantScheduleRepository;

    @Mock
    private VacationGrantRepository vacationGrantRepository;

    @Mock
    private VacationPolicyStrategyFactory strategyFactory;

    @Mock
    private RepeatGrant repeatGrant;

    @InjectMocks
    private VacationGrantScheduler scheduler;

    // 테스트용 User 생성 헬퍼 메소드
    private User createTestUser(String id) {
        return User.createUser(
                null, id, "테스트유저", "test@test.com",
                LocalDate.of(1990, 1, 1), "NONE", "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
    }

    @Nested
    @DisplayName("휴가 만료 처리")
    class ExpireVacationsDaily {
        @Test
        @DisplayName("성공 - 만료 대상 휴가를 만료 처리한다")
        void expireVacationsDailySuccess() {
            // given
            User user = createTestUser("user1");
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
            LocalDate today = LocalDate.now();
            User user = createTestUser("user1");
            VacationPolicy policy = VacationPolicy.createRepeatGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL,
                    new BigDecimal("15.0"), YNType.N, RepeatUnit.YEARLY, 1, null, null,
                    LocalDateTime.of(2025, 1, 1, 0, 0), YNType.Y, null,
                    EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            ReflectionTestUtils.setField(policy, "id", 1L);

            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, policy);
            ReflectionTestUtils.setField(schedule, "id", 1L);

            given(strategyFactory.getStrategy(GrantMethod.REPEAT_GRANT)).willReturn(repeatGrant);
            given(vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(any())).willReturn(List.of(schedule));
            // 첫 번째 호출(today.minusDays(1)): expectedGrantDate로 today 반환 → 부여 대상
            // 두 번째 호출(today): newNextGrantDate로 내년 반환
            given(repeatGrant.calculateNextGrantDate(any(), eq(today.minusDays(1)))).willReturn(today);
            given(repeatGrant.calculateNextGrantDate(any(), eq(today))).willReturn(LocalDate.of(2027, 1, 1));
            willDoNothing().given(vacationGrantRepository).saveAll(anyList());

            // when
            scheduler.grantVacationsDaily();

            // then
            then(vacationGrantScheduleRepository).should().findRepeatGrantTargetsForToday(any());
            then(vacationGrantRepository).should().saveAll(anyList());
        }

        @Test
        @DisplayName("성공 - 부여 대상이 없으면 아무 작업도 하지 않는다")
        void grantVacationsDailyEmpty() {
            // given
            given(strategyFactory.getStrategy(GrantMethod.REPEAT_GRANT)).willReturn(repeatGrant);
            given(vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(any())).willReturn(List.of());

            // when
            scheduler.grantVacationsDaily();

            // then
            then(vacationGrantScheduleRepository).should().findRepeatGrantTargetsForToday(any());
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
