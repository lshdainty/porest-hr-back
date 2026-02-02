package com.porest.hr.repository;

import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.common.type.DefaultCompanyType;
import com.porest.hr.user.domain.User;
import com.porest.hr.vacation.domain.VacationGrantSchedule;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.VacationGrantScheduleQueryDslRepository;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
import com.porest.hr.vacation.type.RepeatUnit;
import com.porest.hr.vacation.type.VacationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({VacationGrantScheduleQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 휴가 부여 스케줄 레포지토리 테스트")
class VacationGrantScheduleQueryDslRepositoryTest {
    @Autowired
    private VacationGrantScheduleQueryDslRepository vacationGrantScheduleRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private VacationPolicy repeatPolicy;
    private VacationPolicy manualPolicy;

    // 테스트용 User 생성 헬퍼 메소드
    private User createTestUser(String id, String name, String email) {
        return User.createUser(
                null, id, name, email,
                LocalDate.of(1990, 1, 1), DefaultCompanyType.NONE, "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
    }

    @BeforeEach
    void setUp() {
        user = createTestUser("user1", "테스트유저1", "user1@test.com");
        em.persist(user);

        repeatPolicy = VacationPolicy.createRepeatGrantPolicy(
                "연차", "연차 정책", VacationType.ANNUAL,
                new BigDecimal("15.0"), YNType.N, RepeatUnit.YEARLY, 1, null, null,
                LocalDateTime.of(2025, 1, 1, 0, 0), YNType.Y, null,
                EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(repeatPolicy);

        manualPolicy = VacationPolicy.createManualGrantPolicy(
                "경조사 휴가", "경조사 휴가 정책", VacationType.WEDDING, new BigDecimal("3.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(manualPolicy);
    }

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        @DisplayName("스케줄 저장 성공")
        void saveSuccess() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);

            // when
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // then
            Optional<VacationGrantSchedule> findSchedule = vacationGrantScheduleRepository.findByUserIdAndPolicyId(user.getId(), repeatPolicy.getId());
            assertThat(findSchedule).isPresent();
            assertThat(findSchedule.get().getUser().getId()).isEqualTo("user1");
            assertThat(findSchedule.get().getVacationPolicy().getName()).isEqualTo("연차");
        }

        @Test
        @DisplayName("다음 부여일 포함하여 스케줄 저장 성공")
        void saveWithNextDateSuccess() {
            // given
            LocalDate nextGrantDate = LocalDate.of(2025, 1, 1);
            VacationGrantSchedule schedule = VacationGrantSchedule.createScheduleWithNextDate(user, repeatPolicy, nextGrantDate);

            // when
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // then
            Optional<VacationGrantSchedule> findSchedule = vacationGrantScheduleRepository.findByUserIdAndPolicyId(user.getId(), repeatPolicy.getId());
            assertThat(findSchedule).isPresent();
            assertThat(findSchedule.get().getNextGrantDate()).isEqualTo(nextGrantDate);
        }
    }

    @Nested
    @DisplayName("findByUserIdAndPolicyId")
    class FindByUserIdAndPolicyId {
        @Test
        @DisplayName("사용자 ID와 정책 ID로 스케줄 조회 성공")
        void findByUserIdAndPolicyIdSuccess() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // when
            Optional<VacationGrantSchedule> findSchedule = vacationGrantScheduleRepository.findByUserIdAndPolicyId(
                    user.getId(), repeatPolicy.getId());

            // then
            assertThat(findSchedule).isPresent();
            assertThat(findSchedule.get().getUser().getId()).isEqualTo("user1");
            assertThat(findSchedule.get().getVacationPolicy().getName()).isEqualTo("연차");
        }

        @Test
        @DisplayName("스케줄이 없으면 빈 Optional 반환")
        void findByUserIdAndPolicyIdNotFound() {
            // given
            em.flush();
            em.clear();

            // when
            Optional<VacationGrantSchedule> findSchedule = vacationGrantScheduleRepository.findByUserIdAndPolicyId(
                    user.getId(), repeatPolicy.getId());

            // then
            assertThat(findSchedule).isEmpty();
        }

        @Test
        @DisplayName("삭제된 스케줄은 조회되지 않음")
        void findByUserIdAndPolicyIdDeletedSchedule() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            vacationGrantScheduleRepository.save(schedule);
            schedule.deleteSchedule();
            em.flush();
            em.clear();

            // when
            Optional<VacationGrantSchedule> findSchedule = vacationGrantScheduleRepository.findByUserIdAndPolicyId(
                    user.getId(), repeatPolicy.getId());

            // then
            assertThat(findSchedule).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndPolicyId")
    class ExistsByUserIdAndPolicyId {
        @Test
        @DisplayName("스케줄 존재 여부 확인 - 존재함")
        void existsByUserIdAndPolicyIdTrue() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // when
            boolean exists = vacationGrantScheduleRepository.existsByUserIdAndPolicyId(user.getId(), repeatPolicy.getId());

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("스케줄 존재 여부 확인 - 없음")
        void existsByUserIdAndPolicyIdFalse() {
            // given
            em.flush();
            em.clear();

            // when
            boolean exists = vacationGrantScheduleRepository.existsByUserIdAndPolicyId(user.getId(), repeatPolicy.getId());

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("삭제된 스케줄은 존재하지 않음으로 처리")
        void existsByUserIdAndPolicyIdDeletedSchedule() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            vacationGrantScheduleRepository.save(schedule);
            schedule.deleteSchedule();
            em.flush();
            em.clear();

            // when
            boolean exists = vacationGrantScheduleRepository.existsByUserIdAndPolicyId(user.getId(), repeatPolicy.getId());

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findRepeatGrantTargetsForToday")
    class FindRepeatGrantTargetsForToday {
        @Test
        @DisplayName("오늘 부여 대상 스케줄 조회 - nextGrantDate가 null인 경우")
        void findRepeatGrantTargetsForTodayNullNextDate() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // when
            List<VacationGrantSchedule> schedules = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(LocalDate.now());

            // then
            assertThat(schedules).hasSize(1);
            assertThat(schedules.get(0).getUser().getId()).isEqualTo("user1");
        }

        @Test
        @DisplayName("오늘 부여 대상 스케줄 조회 - nextGrantDate가 오늘인 경우")
        void findRepeatGrantTargetsForTodayToday() {
            // given
            LocalDate today = LocalDate.now();
            VacationGrantSchedule schedule = VacationGrantSchedule.createScheduleWithNextDate(user, repeatPolicy, today);
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // when
            List<VacationGrantSchedule> schedules = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(today);

            // then
            assertThat(schedules).hasSize(1);
        }

        @Test
        @DisplayName("오늘 부여 대상 스케줄 조회 - nextGrantDate가 과거인 경우")
        void findRepeatGrantTargetsForTodayPast() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate pastDate = today.minusDays(1);
            VacationGrantSchedule schedule = VacationGrantSchedule.createScheduleWithNextDate(user, repeatPolicy, pastDate);
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // when
            List<VacationGrantSchedule> schedules = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(today);

            // then
            assertThat(schedules).hasSize(1);
        }

        @Test
        @DisplayName("오늘 부여 대상 스케줄 조회 - nextGrantDate가 미래인 경우 제외")
        void findRepeatGrantTargetsForTodayFuture() {
            // given
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(1);
            VacationGrantSchedule schedule = VacationGrantSchedule.createScheduleWithNextDate(user, repeatPolicy, futureDate);
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // when
            List<VacationGrantSchedule> schedules = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(today);

            // then
            assertThat(schedules).isEmpty();
        }

        @Test
        @DisplayName("오늘 부여 대상 스케줄 조회 - MANUAL_GRANT 정책은 제외")
        void findRepeatGrantTargetsForTodayExcludesManual() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, manualPolicy);
            vacationGrantScheduleRepository.save(schedule);
            em.flush();
            em.clear();

            // when
            List<VacationGrantSchedule> schedules = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(LocalDate.now());

            // then
            assertThat(schedules).isEmpty();
        }

        @Test
        @DisplayName("오늘 부여 대상 스케줄 조회 - 삭제된 스케줄 제외")
        void findRepeatGrantTargetsForTodayExcludesDeleted() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            vacationGrantScheduleRepository.save(schedule);
            schedule.deleteSchedule();
            em.flush();
            em.clear();

            // when
            List<VacationGrantSchedule> schedules = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(LocalDate.now());

            // then
            assertThat(schedules).isEmpty();
        }

        @Test
        @DisplayName("오늘 부여 대상 스케줄 조회 - 삭제된 정책 제외")
        void findRepeatGrantTargetsForTodayExcludesDeletedPolicy() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            vacationGrantScheduleRepository.save(schedule);
            repeatPolicy.deleteVacationPolicy();
            em.flush();
            em.clear();

            // when
            List<VacationGrantSchedule> schedules = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(LocalDate.now());

            // then
            assertThat(schedules).isEmpty();
        }

        @Test
        @DisplayName("오늘 부여 대상 스케줄 조회 - 삭제된 사용자 제외")
        void findRepeatGrantTargetsForTodayExcludesDeletedUser() {
            // given
            VacationGrantSchedule schedule = VacationGrantSchedule.createSchedule(user, repeatPolicy);
            vacationGrantScheduleRepository.save(schedule);
            user.deleteUser();
            em.flush();
            em.clear();

            // when
            List<VacationGrantSchedule> schedules = vacationGrantScheduleRepository.findRepeatGrantTargetsForToday(LocalDate.now());

            // then
            assertThat(schedules).isEmpty();
        }
    }
}
