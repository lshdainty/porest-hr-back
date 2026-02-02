package com.porest.hr.repository;

import com.porest.core.type.YNType;
import com.porest.hr.vacation.domain.VacationPlan;
import com.porest.hr.vacation.domain.VacationPlanPolicy;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.VacationPlanJpaRepository;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
import com.porest.hr.vacation.type.VacationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({VacationPlanJpaRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPQL 휴가 플랜 레포지토리 테스트")
class VacationPlanJpaRepositoryTest {
    @Autowired
    private VacationPlanJpaRepository vacationPlanRepository;

    @Autowired
    private TestEntityManager em;

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        @DisplayName("휴가 플랜 저장 성공")
        void saveSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");

            // when
            vacationPlanRepository.save(plan);
            em.flush();
            em.clear();

            // then
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCode("DEFAULT");
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getName()).isEqualTo("기본 플랜");
        }
    }

    @Nested
    @DisplayName("findByIdWithPolicies")
    class FindByIdWithPolicies {
        @Test
        @DisplayName("ID로 플랜 조회 시 정책도 함께 조회")
        void findByIdWithPoliciesSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            em.persist(plan);
            em.persist(policy);
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.Y);
            em.persist(planPolicy);
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByIdWithPolicies(plan.getId());

            // then
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getVacationPlanPolicies()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
        void findByIdWithPoliciesNotFound() {
            // given & when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByIdWithPolicies(999L);

            // then
            assertThat(findPlan).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCode")
    class FindByCode {
        @Test
        @DisplayName("코드로 플랜 조회 성공")
        void findByCodeSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("SENIOR", "선임 플랜", "선임용 플랜");
            em.persist(plan);
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCode("SENIOR");

            // then
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getName()).isEqualTo("선임 플랜");
        }

        @Test
        @DisplayName("존재하지 않는 코드로 조회 시 빈 Optional 반환")
        void findByCodeNotFound() {
            // given & when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCode("NOT_EXISTS");

            // then
            assertThat(findPlan).isEmpty();
        }

        @Test
        @DisplayName("삭제된 플랜은 조회되지 않음")
        void findByCodeDeletedPlan() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
            em.persist(plan);
            plan.deletePlan();
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCode("DEFAULT");

            // then
            assertThat(findPlan).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCodeWithPolicies")
    class FindByCodeWithPolicies {
        @Test
        @DisplayName("코드로 플랜 조회 시 정책도 함께 조회")
        void findByCodeWithPoliciesSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            em.persist(plan);
            em.persist(policy);
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.Y);
            em.persist(planPolicy);
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCodeWithPolicies("DEFAULT");

            // then
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getVacationPlanPolicies()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 코드로 조회 시 빈 Optional 반환")
        void findByCodeWithPoliciesNotFound() {
            // given & when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCodeWithPolicies("NOT_EXISTS");

            // then
            assertThat(findPlan).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllWithPolicies")
    class FindAllWithPolicies {
        @Test
        @DisplayName("모든 플랜 조회 시 정책도 함께 조회")
        void findAllWithPoliciesSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            em.persist(plan);
            em.persist(policy);
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.Y);
            em.persist(planPolicy);
            em.flush();
            em.clear();

            // when
            List<VacationPlan> plans = vacationPlanRepository.findAllWithPolicies();

            // then
            assertThat(plans).hasSize(1);
            assertThat(plans.get(0).getVacationPlanPolicies()).hasSize(1);
        }

        @Test
        @DisplayName("플랜이 없으면 빈 리스트 반환")
        void findAllWithPoliciesEmpty() {
            // given & when
            List<VacationPlan> plans = vacationPlanRepository.findAllWithPolicies();

            // then
            assertThat(plans).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByCode")
    class ExistsByCode {
        @Test
        @DisplayName("플랜 존재 여부 확인 - 존재함")
        void existsByCodeTrue() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
            em.persist(plan);
            em.flush();
            em.clear();

            // when
            boolean exists = vacationPlanRepository.existsByCode("DEFAULT");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("플랜 존재 여부 확인 - 없음")
        void existsByCodeFalse() {
            // given & when
            boolean exists = vacationPlanRepository.existsByCode("NOT_EXISTS");

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("삭제된 플랜은 존재하지 않음으로 처리")
        void existsByCodeDeletedPlan() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
            em.persist(plan);
            plan.deletePlan();
            em.flush();
            em.clear();

            // when
            boolean exists = vacationPlanRepository.existsByCode("DEFAULT");

            // then
            assertThat(exists).isFalse();
        }
    }
}
