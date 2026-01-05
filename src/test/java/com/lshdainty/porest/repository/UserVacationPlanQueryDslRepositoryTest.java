package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.UserVacationPlan;
import com.lshdainty.porest.vacation.domain.VacationPlan;
import com.lshdainty.porest.vacation.domain.VacationPlanPolicy;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.UserVacationPlanQueryDslRepository;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
import com.lshdainty.porest.vacation.type.VacationType;
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
@Import({UserVacationPlanQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 사용자-휴가 플랜 매핑 레포지토리 테스트")
class UserVacationPlanQueryDslRepositoryTest {
    @Autowired
    private UserVacationPlanQueryDslRepository userVacationPlanRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private VacationPlan plan;

    @BeforeEach
    void setUp() {
        user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 18",
                YNType.N, null, null, CountryCode.KR
        );
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
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);

            // when
            userVacationPlanRepository.save(userPlan);
            em.flush();
            em.clear();

            // then
            Optional<UserVacationPlan> findUserPlan = userVacationPlanRepository.findByUserIdAndPlanCode(user.getId(), "DEFAULT");
            assertThat(findUserPlan).isPresent();
            assertThat(findUserPlan.get().getUser().getId()).isEqualTo("user1");
            assertThat(findUserPlan.get().getVacationPlan().getCode()).isEqualTo("DEFAULT");
        }
    }

    @Nested
    @DisplayName("findByUserIdWithPlanAndPolicies")
    class FindByUserIdWithPlanAndPolicies {
        @Test
        @DisplayName("사용자 ID로 매핑, 플랜, 정책 모두 함께 조회 성공")
        void findByUserIdWithPlanAndPoliciesSuccess() {
            // given
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            em.persist(policy);

            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.N);
            em.persist(planPolicy);

            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userPlan);
            em.flush();
            em.clear();

            // when
            List<UserVacationPlan> userPlans = userVacationPlanRepository.findByUserIdWithPlanAndPolicies(user.getId());

            // then
            assertThat(userPlans).hasSize(1);
            assertThat(userPlans.get(0).getVacationPlan().getVacationPlanPolicies()).hasSize(1);
        }

        @Test
        @DisplayName("정책이 없는 플랜도 조회 가능")
        void findByUserIdWithPlanAndPoliciesEmptyPolicies() {
            // given
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userPlan);
            em.flush();
            em.clear();

            // when
            List<UserVacationPlan> userPlans = userVacationPlanRepository.findByUserIdWithPlanAndPolicies(user.getId());

            // then
            assertThat(userPlans).hasSize(1);
            assertThat(userPlans.get(0).getVacationPlan().getVacationPlanPolicies()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndPlanCode")
    class FindByUserIdAndPlanCode {
        @Test
        @DisplayName("사용자 ID와 플랜 코드로 매핑 조회 성공")
        void findByUserIdAndPlanCodeSuccess() {
            // given
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userPlan);
            em.flush();
            em.clear();

            // when
            Optional<UserVacationPlan> findUserPlan = userVacationPlanRepository.findByUserIdAndPlanCode(
                    user.getId(), "DEFAULT");

            // then
            assertThat(findUserPlan).isPresent();
            assertThat(findUserPlan.get().getVacationPlan().getName()).isEqualTo("기본 플랜");
        }

        @Test
        @DisplayName("매핑이 없으면 빈 Optional 반환")
        void findByUserIdAndPlanCodeNotFound() {
            // given
            em.flush();
            em.clear();

            // when
            Optional<UserVacationPlan> findUserPlan = userVacationPlanRepository.findByUserIdAndPlanCode(
                    user.getId(), "NOT_EXISTS");

            // then
            assertThat(findUserPlan).isEmpty();
        }

        @Test
        @DisplayName("삭제된 플랜은 조회되지 않음")
        void findByUserIdAndPlanCodeDeletedPlan() {
            // given
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userPlan);
            plan.deletePlan();
            em.flush();
            em.clear();

            // when
            Optional<UserVacationPlan> findUserPlan = userVacationPlanRepository.findByUserIdAndPlanCode(
                    user.getId(), "DEFAULT");

            // then
            assertThat(findUserPlan).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByUserIdAndPlanCode")
    class ExistsByUserIdAndPlanCode {
        @Test
        @DisplayName("매핑 존재 여부 확인 - 존재함")
        void existsByUserIdAndPlanCodeTrue() {
            // given
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userPlan);
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
            boolean exists = userVacationPlanRepository.existsByUserIdAndPlanCode(user.getId(), "NOT_EXISTS");

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("삭제된 플랜은 존재하지 않음으로 처리")
        void existsByUserIdAndPlanCodeDeletedPlan() {
            // given
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userPlan);
            plan.deletePlan();
            em.flush();
            em.clear();

            // when
            boolean exists = userVacationPlanRepository.existsByUserIdAndPlanCode(user.getId(), "DEFAULT");

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("삭제된 매핑은 존재하지 않음으로 처리")
        void existsByUserIdAndPlanCodeDeletedUserPlan() {
            // given
            UserVacationPlan userPlan = UserVacationPlan.createUserVacationPlan(user, plan);
            userVacationPlanRepository.save(userPlan);
            userPlan.deleteUserVacationPlan();
            em.flush();
            em.clear();

            // when
            boolean exists = userVacationPlanRepository.existsByUserIdAndPlanCode(user.getId(), "DEFAULT");

            // then
            assertThat(exists).isFalse();
        }
    }
}
