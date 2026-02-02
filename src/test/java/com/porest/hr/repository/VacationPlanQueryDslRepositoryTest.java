package com.porest.hr.repository;

import com.porest.core.type.YNType;
import com.porest.hr.vacation.domain.VacationPlan;
import com.porest.hr.vacation.domain.VacationPlanPolicy;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.repository.VacationPlanQueryDslRepository;
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
@Import({VacationPlanQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 휴가 플랜 레포지토리 테스트")
class VacationPlanQueryDslRepositoryTest {
    @Autowired
    private VacationPlanQueryDslRepository vacationPlanRepository;

    @Autowired
    private TestEntityManager em;

    @Nested
    @DisplayName("save")
    class Save {
        @Test
        @DisplayName("휴가 플랜 저장 및 조회 성공")
        void saveAndFindSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("FULL_TIME", "정규직 플랜", "정규직 직원용 휴가 플랜");

            // when
            vacationPlanRepository.save(plan);
            em.flush();
            em.clear();

            // then
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCode("FULL_TIME");
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getCode()).isEqualTo("FULL_TIME");
            assertThat(findPlan.get().getName()).isEqualTo("정규직 플랜");
            assertThat(findPlan.get().getDesc()).isEqualTo("정규직 직원용 휴가 플랜");
        }
    }

    @Nested
    @DisplayName("findByIdWithPolicies")
    class FindByIdWithPolicies {
        @Test
        @DisplayName("ID로 플랜과 정책 함께 조회 성공")
        void findByIdWithPoliciesSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본 휴가 플랜");
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            em.persist(policy);
            vacationPlanRepository.save(plan);
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.N);
            em.persist(planPolicy);
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByIdWithPolicies(plan.getId());

            // then
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getVacationPlanPolicies()).hasSize(1);
            assertThat(findPlan.get().getVacationPlanPolicies().get(0).getVacationPolicy().getName()).isEqualTo("연차");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
        void findByIdWithPoliciesNotFound() {
            // given & when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByIdWithPolicies(999L);

            // then
            assertThat(findPlan).isEmpty();
        }

        @Test
        @DisplayName("정책이 없는 플랜도 조회 가능")
        void findByIdWithPoliciesEmptyPolicies() {
            // given
            VacationPlan plan = VacationPlan.createPlan("EMPTY", "빈 플랜", "정책 없는 플랜");
            vacationPlanRepository.save(plan);
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByIdWithPolicies(plan.getId());

            // then
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getVacationPlanPolicies()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCode")
    class FindByCode {
        @Test
        @DisplayName("코드로 플랜 조회 성공")
        void findByCodeSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("CONTRACT", "계약직 플랜", "계약직 직원용 플랜");
            vacationPlanRepository.save(plan);
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCode("CONTRACT");

            // then
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getName()).isEqualTo("계약직 플랜");
        }

        @Test
        @DisplayName("존재하지 않는 코드로 조회 시 빈 Optional 반환")
        void findByCodeNotFound() {
            // given & when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCode("NOT_EXIST");

            // then
            assertThat(findPlan).isEmpty();
        }

        @Test
        @DisplayName("삭제된 플랜은 코드로 조회되지 않음")
        void findByCodeDeletedPlan() {
            // given
            VacationPlan plan = VacationPlan.createPlan("DELETED", "삭제된 플랜", "삭제된 플랜");
            vacationPlanRepository.save(plan);
            plan.deletePlan();
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCode("DELETED");

            // then
            assertThat(findPlan).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCodeWithPolicies")
    class FindByCodeWithPolicies {
        @Test
        @DisplayName("코드로 플랜과 정책 함께 조회 성공")
        void findByCodeWithPoliciesSuccess() {
            // given
            VacationPlan plan = VacationPlan.createPlan("SENIOR", "7년차 플랜", "7년 이상 근속자용 플랜");
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "추가 연차", "7년차 추가 연차", VacationType.ANNUAL, new BigDecimal("5.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            em.persist(policy);
            vacationPlanRepository.save(plan);
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan, policy, 1, YNType.N);
            em.persist(planPolicy);
            em.flush();
            em.clear();

            // when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCodeWithPolicies("SENIOR");

            // then
            assertThat(findPlan).isPresent();
            assertThat(findPlan.get().getVacationPlanPolicies()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 코드로 조회 시 빈 Optional 반환")
        void findByCodeWithPoliciesNotFound() {
            // given & when
            Optional<VacationPlan> findPlan = vacationPlanRepository.findByCodeWithPolicies("NOT_EXIST");

            // then
            assertThat(findPlan).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllWithPolicies")
    class FindAllWithPolicies {
        @Test
        @DisplayName("전체 플랜과 정책 함께 조회 성공")
        void findAllWithPoliciesSuccess() {
            // given
            VacationPlan plan1 = VacationPlan.createPlan("DEFAULT", "기본 플랜", "기본");
            VacationPlan plan2 = VacationPlan.createPlan("SENIOR", "선임 플랜", "선임용");
            VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                    "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                    YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
            );
            em.persist(policy);
            vacationPlanRepository.save(plan1);
            vacationPlanRepository.save(plan2);
            VacationPlanPolicy planPolicy = VacationPlanPolicy.createPlanPolicy(plan1, policy, 1, YNType.N);
            em.persist(planPolicy);
            em.flush();
            em.clear();

            // when
            List<VacationPlan> plans = vacationPlanRepository.findAllWithPolicies();

            // then
            assertThat(plans).hasSize(2);
        }

        @Test
        @DisplayName("플랜이 없으면 빈 리스트 반환")
        void findAllWithPoliciesEmpty() {
            // given & when
            List<VacationPlan> plans = vacationPlanRepository.findAllWithPolicies();

            // then
            assertThat(plans).isEmpty();
        }

        @Test
        @DisplayName("삭제된 플랜은 조회에서 제외")
        void findAllWithPoliciesExcludesDeleted() {
            // given
            VacationPlan activePlan = VacationPlan.createPlan("ACTIVE", "활성 플랜", "활성");
            VacationPlan deletedPlan = VacationPlan.createPlan("DELETED", "삭제된 플랜", "삭제됨");
            vacationPlanRepository.save(activePlan);
            vacationPlanRepository.save(deletedPlan);
            deletedPlan.deletePlan();
            em.flush();
            em.clear();

            // when
            List<VacationPlan> plans = vacationPlanRepository.findAllWithPolicies();

            // then
            assertThat(plans).hasSize(1);
            assertThat(plans.get(0).getCode()).isEqualTo("ACTIVE");
        }
    }

    @Nested
    @DisplayName("existsByCode")
    class ExistsByCode {
        @Test
        @DisplayName("코드 존재 여부 확인 - 존재함")
        void existsByCodeTrue() {
            // given
            vacationPlanRepository.save(VacationPlan.createPlan("EXISTS", "존재하는 플랜", "존재"));
            em.flush();
            em.clear();

            // when
            boolean exists = vacationPlanRepository.existsByCode("EXISTS");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("코드 존재 여부 확인 - 없음")
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
            VacationPlan plan = VacationPlan.createPlan("DELETED", "삭제된 플랜", "삭제됨");
            vacationPlanRepository.save(plan);
            plan.deletePlan();
            em.flush();
            em.clear();

            // when
            boolean exists = vacationPlanRepository.existsByCode("DELETED");

            // then
            assertThat(exists).isFalse();
        }
    }
}
