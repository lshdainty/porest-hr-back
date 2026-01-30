package com.porest.hr.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.porest.hr.user.domain.User;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.porest.hr.vacation.domain.UserVacationPlan;
import com.porest.hr.vacation.domain.VacationPlan;
import com.porest.hr.vacation.domain.VacationPlanPolicy;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.UserVacationPlanJpaRepository;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({UserVacationPlanJpaRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPQL 사용자-휴가 플랜 매핑 레포지토리 테스트")
class UserVacationPlanJpaRepositoryTest {
    @Autowired
    private UserVacationPlanJpaRepository userVacationPlanRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private VacationPlan plan;

    // 테스트용 User 생성 헬퍼 메소드
    private User createTestUser(String id, String name, String email) {
        return User.createUser(
                null, id, name, email,
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
    }

    @BeforeEach
    void setUp() {
        user = createTestUser("user1", "테스트유저1", "user1@test.com");
        plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
        em.persist(user);
        em.persist(plan);
    }

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        @DisplayName("사용자-플랜 매핑 저장 성공")
        void saveSuccess() {
            // given
            UserVacationPlan userVacationPlan = UserVacationPlan.createUserVacationPlan(user, plan);

            // when
            userVacationPlanRepository.save(userVacationPlan);
            em.flush();
            em.clear();

            // then
            Optional<UserVacationPlan> findUserVacationPlan = userVacationPlanRepository.findByUserIdAndPlanCode(user.getId(), "DEFAULT");
            assertThat(findUserVacationPlan).isPresent();
            assertThat(findUserVacationPlan.get().getVacationPlan().getCode()).isEqualTo("DEFAULT");
        }
    }

    @Nested
    @DisplayName("findByUserIdWithPlanAndPolicies")
    class FindByUserIdWithPlanAndPolicies {
        @Test
        @DisplayName("사용자 ID로 매핑 조회 시 플랜과 정책도 함께 조회")
        void findByUserIdWithPlanAndPoliciesSuccess() {
            // given
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            em.persist(policy);
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.Y);
            em.persist(planPolicy);
            UserVacationPlan userVacationPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userVacationPlan);
            em.flush();
            em.clear();

            // when
            List<UserVacationPlan> userVacationPlans = userVacationPlanRepository.findByUserIdWithPlanAndPolicies(user.getId());

            // then
            assertThat(userVacationPlans).hasSize(1);
            assertThat(userVacationPlans.get(0).getVacationPlan().getVacationPlanPolicies()).hasSize(1);
        }

        @Test
        @DisplayName("사용자에게 할당된 플랜이 없으면 빈 리스트 반환")
        void findByUserIdWithPlanAndPoliciesEmpty() {
            // given
            em.flush();
            em.clear();

            // when
            List<UserVacationPlan> userVacationPlans = userVacationPlanRepository.findByUserIdWithPlanAndPolicies(user.getId());

            // then
            assertThat(userVacationPlans).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndPlanCode")
    class FindByUserIdAndPlanCode {
        @Test
        @DisplayName("사용자 ID와 플랜 코드로 매핑 조회 성공")
        void findByUserIdAndPlanCodeSuccess() {
            // given
            UserVacationPlan userVacationPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userVacationPlan);
            em.flush();
            em.clear();

            // when
            Optional<UserVacationPlan> findUserVacationPlan = userVacationPlanRepository.findByUserIdAndPlanCode(
                    user.getId(), "DEFAULT");

            // then
            assertThat(findUserVacationPlan).isPresent();
            assertThat(findUserVacationPlan.get().getVacationPlan().getName()).isEqualTo("기본 플랜");
        }

        @Test
        @DisplayName("매핑이 없으면 빈 Optional 반환")
        void findByUserIdAndPlanCodeNotFound() {
            // given
            em.flush();
            em.clear();

            // when
            Optional<UserVacationPlan> findUserVacationPlan = userVacationPlanRepository.findByUserIdAndPlanCode(
                    user.getId(), "DEFAULT");

            // then
            assertThat(findUserVacationPlan).isEmpty();
        }

        @Test
        @DisplayName("삭제된 매핑은 조회되지 않음")
        void findByUserIdAndPlanCodeDeletedUserVacationPlan() {
            // given
            UserVacationPlan userVacationPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userVacationPlan);
            userVacationPlan.deleteUserVacationPlan();
            em.flush();
            em.clear();

            // when
            Optional<UserVacationPlan> findUserVacationPlan = userVacationPlanRepository.findByUserIdAndPlanCode(
                    user.getId(), "DEFAULT");

            // then
            assertThat(findUserVacationPlan).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndPlanCode")
    class ExistsByUserIdAndPlanCode {
        @Test
        @DisplayName("매핑 존재 여부 확인 - 존재함")
        void existsByUserIdAndPlanCodeTrue() {
            // given
            UserVacationPlan userVacationPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userVacationPlan);
            em.flush();
            em.clear();

            // when
            boolean exists = userVacationPlanRepository.existsByUserIdAndPlanCode(user.getId(), "DEFAULT");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("매핑 존재 여부 확인 - 없음")
        void existsByUserIdAndPlanCodeFalse() {
            // given
            em.flush();
            em.clear();

            // when
            boolean exists = userVacationPlanRepository.existsByUserIdAndPlanCode(user.getId(), "DEFAULT");

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("삭제된 매핑은 존재하지 않음으로 처리")
        void existsByUserIdAndPlanCodeDeletedUserVacationPlan() {
            // given
            UserVacationPlan userVacationPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userVacationPlan);
            userVacationPlan.deleteUserVacationPlan();
            em.flush();
            em.clear();

            // when
            boolean exists = userVacationPlanRepository.existsByUserIdAndPlanCode(user.getId(), "DEFAULT");

            // then
            assertThat(exists).isFalse();
        }
    }
}
