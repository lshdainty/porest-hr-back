package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationPolicyQueryDslRepository;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
import com.lshdainty.porest.vacation.type.VacationType;
import org.junit.jupiter.api.DisplayName;
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
@Import({VacationPolicyQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 휴가정책 레포지토리 테스트")
class VacationPolicyQueryDslRepositoryTest {
    @Autowired
    private VacationPolicyQueryDslRepository vacationPolicyRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("휴가정책 저장 및 단건 조회")
    void save() {
        // given
        VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                "연차 휴가", "연차 휴가 정책", VacationType.ANNUAL, new BigDecimal("8.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );

        // when
        vacationPolicyRepository.save(policy);
        em.flush();
        em.clear();

        // then
        Optional<VacationPolicy> findPolicy = vacationPolicyRepository.findVacationPolicyById(policy.getId());
        assertThat(findPolicy.isPresent()).isTrue();
        assertThat(findPolicy.get().getName()).isEqualTo("연차 휴가");
        assertThat(findPolicy.get().getVacationType()).isEqualTo(VacationType.ANNUAL);
    }

    @Test
    @DisplayName("단건 조회 시 정책이 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<VacationPolicy> findPolicy = vacationPolicyRepository.findVacationPolicyById(999L);

        // then
        assertThat(findPolicy.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("전체 휴가정책 조회")
    void findVacationPolicies() {
        // given
        vacationPolicyRepository.save(VacationPolicy.createManualGrantPolicy(
                "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("8.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        ));
        vacationPolicyRepository.save(VacationPolicy.createManualGrantPolicy(
                "건강휴가", "건강검진 휴가", VacationType.HEALTH, new BigDecimal("4.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        ));
        em.flush();
        em.clear();

        // when
        List<VacationPolicy> policies = vacationPolicyRepository.findVacationPolicies();

        // then
        assertThat(policies).hasSize(2);
        assertThat(policies).extracting("name").containsExactlyInAnyOrder("연차", "건강휴가");
    }

    @Test
    @DisplayName("전체 휴가정책 조회 시 삭제된 정책 제외")
    void findVacationPoliciesExcludesDeleted() {
        // given
        VacationPolicy activePolicy = VacationPolicy.createManualGrantPolicy(
                "활성 정책", "활성", VacationType.ANNUAL, new BigDecimal("8.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        VacationPolicy deletedPolicy = VacationPolicy.createManualGrantPolicy(
                "삭제 정책", "삭제", VacationType.HEALTH, new BigDecimal("4.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        vacationPolicyRepository.save(activePolicy);
        vacationPolicyRepository.save(deletedPolicy);
        deletedPolicy.deleteVacationPolicy();
        em.flush();
        em.clear();

        // when
        List<VacationPolicy> policies = vacationPolicyRepository.findVacationPolicies();

        // then
        assertThat(policies).hasSize(1);
        assertThat(policies.get(0).getName()).isEqualTo("활성 정책");
    }

    @Test
    @DisplayName("전체 휴가정책이 없어도 Null이 반환되면 안된다.")
    void findVacationPoliciesEmpty() {
        // given & when
        List<VacationPolicy> policies = vacationPolicyRepository.findVacationPolicies();

        // then
        assertThat(policies.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("정책명 존재 여부 확인 - 존재함")
    void existsByNameTrue() {
        // given
        vacationPolicyRepository.save(VacationPolicy.createManualGrantPolicy(
                "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("8.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        ));
        em.flush();
        em.clear();

        // when
        boolean exists = vacationPolicyRepository.existsByName("연차");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("정책명 존재 여부 확인 - 없음")
    void existsByNameFalse() {
        // given & when
        boolean exists = vacationPolicyRepository.existsByName("존재하지 않는 정책");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("삭제된 정책명은 존재하지 않음으로 처리")
    void existsByNameDeletedPolicy() {
        // given
        VacationPolicy policy = VacationPolicy.createManualGrantPolicy(
                "삭제정책", "삭제", VacationType.ANNUAL, new BigDecimal("8.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        vacationPolicyRepository.save(policy);
        policy.deleteVacationPolicy();
        em.flush();
        em.clear();

        // when
        boolean exists = vacationPolicyRepository.existsByName("삭제정책");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("ON_REQUEST 타입 정책 생성 및 조회")
    void createOnRequestPolicy() {
        // given
        VacationPolicy policy = VacationPolicy.createOnRequestPolicy(
                "OT 휴가", "야근 대체 휴가", VacationType.OVERTIME, new BigDecimal("8.0"),
                YNType.Y, YNType.N, 2, EffectiveType.IMMEDIATELY, ExpirationType.THREE_MONTHS_AFTER_GRANT
        );
        vacationPolicyRepository.save(policy);
        em.flush();
        em.clear();

        // when
        Optional<VacationPolicy> findPolicy = vacationPolicyRepository.findVacationPolicyById(policy.getId());

        // then
        assertThat(findPolicy.isPresent()).isTrue();
        assertThat(findPolicy.get().getApprovalRequiredCount()).isEqualTo(2);
    }
}
