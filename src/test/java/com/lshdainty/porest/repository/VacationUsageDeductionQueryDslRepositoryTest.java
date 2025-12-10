package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import com.lshdainty.porest.vacation.domain.VacationUsageDeduction;
import com.lshdainty.porest.vacation.repository.VacationUsageDeductionQueryDslRepository;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
import com.lshdainty.porest.vacation.type.VacationTimeType;
import com.lshdainty.porest.vacation.type.VacationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({VacationUsageDeductionQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 휴가사용차감 레포지토리 테스트")
class VacationUsageDeductionQueryDslRepositoryTest {
    @Autowired
    private VacationUsageDeductionQueryDslRepository deductionRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private VacationPolicy policy;
    private VacationGrant grant;
    private VacationUsage usage;

    @BeforeEach
    void setUp() {
        user = User.createUser("user1");
        em.persist(user);

        policy = VacationPolicy.createManualGrantPolicy(
                "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(policy);

        grant = VacationGrant.createVacationGrant(
                user, policy, "2025년 연차", VacationType.ANNUAL, new BigDecimal("15.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59)
        );
        em.persist(grant);

        usage = VacationUsage.createVacationUsage(
                user, "연차 사용", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        );
        em.persist(usage);
    }

    @Test
    @DisplayName("차감 저장")
    void save() {
        // given
        VacationUsageDeduction deduction = VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        );

        // when
        deductionRepository.save(deduction);
        em.flush();
        em.clear();

        // then
        List<VacationUsageDeduction> result = deductionRepository.findByUsageId(usage.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeductedTime()).isEqualByComparingTo(new BigDecimal("1.0000"));
    }

    @Test
    @DisplayName("차감 다건 저장")
    void saveAll() {
        // given
        VacationGrant grant2 = VacationGrant.createVacationGrant(
                user, policy, "추가 연차", VacationType.ANNUAL, new BigDecimal("5.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59)
        );
        em.persist(grant2);

        List<VacationUsageDeduction> deductions = List.of(
                VacationUsageDeduction.createVacationUsageDeduction(usage, grant, new BigDecimal("0.5")),
                VacationUsageDeduction.createVacationUsageDeduction(usage, grant2, new BigDecimal("0.5"))
        );

        // when
        deductionRepository.saveAll(deductions);
        em.flush();
        em.clear();

        // then
        List<VacationUsageDeduction> result = deductionRepository.findByUsageId(usage.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("사용 ID로 차감 조회")
    void findByUsageId() {
        // given
        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> result = deductionRepository.findByUsageId(usage.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsage()).isNotNull();
        assertThat(result.get(0).getGrant()).isNotNull();
    }

    @Test
    @DisplayName("부여 ID로 차감 조회")
    void findByGrantId() {
        // given
        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> result = deductionRepository.findByGrantId(grant.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsage()).isNotNull();
        assertThat(result.get(0).getGrant()).isNotNull();
    }

    @Test
    @DisplayName("사용 ID로 조회 시 없으면 빈 리스트 반환")
    void findByUsageIdEmpty() {
        // when
        List<VacationUsageDeduction> result = deductionRepository.findByUsageId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("부여 ID로 조회 시 없으면 빈 리스트 반환")
    void findByGrantIdEmpty() {
        // when
        List<VacationUsageDeduction> result = deductionRepository.findByGrantId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 사용에 대한 차감 저장 및 조회")
    void saveMultipleUsageDeductions() {
        // given
        VacationUsage usage2 = VacationUsage.createVacationUsage(
                user, "연차 사용2", VacationTimeType.MORNINGOFF,
                LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 13, 0),
                new BigDecimal("0.5000")
        );
        em.persist(usage2);

        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        ));
        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                usage2, grant, new BigDecimal("0.5000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> resultByGrant = deductionRepository.findByGrantId(grant.getId());
        List<VacationUsageDeduction> resultByUsage1 = deductionRepository.findByUsageId(usage.getId());
        List<VacationUsageDeduction> resultByUsage2 = deductionRepository.findByUsageId(usage2.getId());

        // then
        assertThat(resultByGrant).hasSize(2);
        assertThat(resultByUsage1).hasSize(1);
        assertThat(resultByUsage2).hasSize(1);
    }

    @Test
    @DisplayName("여러 부여 ID로 차감 내역 일괄 조회")
    void findByGrantIds() {
        // given
        VacationGrant grant2 = VacationGrant.createVacationGrant(
                user, policy, "추가 연차", VacationType.ANNUAL, new BigDecimal("5.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59)
        );
        em.persist(grant2);

        VacationUsage usage2 = VacationUsage.createVacationUsage(
                user, "연차 사용2", VacationTimeType.MORNINGOFF,
                LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 13, 0),
                new BigDecimal("0.5000")
        );
        em.persist(usage2);

        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        ));
        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                usage2, grant2, new BigDecimal("0.5000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> result = deductionRepository.findByGrantIds(List.of(grant.getId(), grant2.getId()));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(d -> d.getUsage().getId())
                .containsExactlyInAnyOrder(usage.getId(), usage2.getId());
    }

    @Test
    @DisplayName("여러 부여 ID로 조회 시 빈 리스트 전달하면 빈 결과 반환")
    void findByGrantIdsEmpty() {
        // when
        List<VacationUsageDeduction> result = deductionRepository.findByGrantIds(List.of());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 부여 ID로 조회 시 null 전달하면 빈 결과 반환")
    void findByGrantIdsNull() {
        // when
        List<VacationUsageDeduction> result = deductionRepository.findByGrantIds(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 부여 ID로 조회 시 삭제된 사용 내역은 제외")
    void findByGrantIdsExcludesDeletedUsage() {
        // given
        VacationUsage deletedUsage = VacationUsage.createVacationUsage(
                user, "삭제된 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 3, 9, 0), LocalDateTime.of(2025, 6, 3, 18, 0),
                new BigDecimal("1.0000")
        );
        em.persist(deletedUsage);
        deletedUsage.deleteVacationUsage();

        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        ));
        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                deletedUsage, grant, new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> result = deductionRepository.findByGrantIds(List.of(grant.getId()));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsage().getId()).isEqualTo(usage.getId());
    }

    @Test
    @DisplayName("여러 부여 ID로 조회 시 존재하지 않는 ID가 포함되어도 정상 조회")
    void findByGrantIdsWithNonExistentId() {
        // given
        deductionRepository.save(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> result = deductionRepository.findByGrantIds(List.of(grant.getId(), 999L));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGrant().getId()).isEqualTo(grant.getId());
    }
}
