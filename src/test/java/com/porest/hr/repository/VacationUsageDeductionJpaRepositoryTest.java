package com.porest.hr.repository;

import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.common.type.DefaultCompanyType;
import com.porest.hr.user.domain.User;
import com.porest.hr.vacation.domain.VacationGrant;
import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.domain.VacationUsage;
import com.porest.hr.vacation.domain.VacationUsageDeduction;
import com.porest.hr.vacation.repository.VacationUsageDeductionJpaRepository;
import com.porest.hr.vacation.type.EffectiveType;
import com.porest.hr.vacation.type.ExpirationType;
import com.porest.hr.vacation.type.VacationTimeType;
import com.porest.hr.vacation.type.VacationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({VacationUsageDeductionJpaRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 휴가사용차감 레포지토리 테스트")
class VacationUsageDeductionJpaRepositoryTest {
    @Autowired
    private VacationUsageDeductionJpaRepository deductionRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private VacationPolicy policy;
    private VacationGrant grant;
    private VacationUsage usage;

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
        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        )));
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
    @DisplayName("사용 ID로 조회 시 없으면 빈 리스트 반환")
    void findByUsageIdEmpty() {
        // when
        List<VacationUsageDeduction> result = deductionRepository.findByUsageId(999L);

        // then
        assertThat(result).isEmpty();
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

        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        )));
        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                usage2, grant2, new BigDecimal("0.5000")
        )));
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

        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        )));
        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                deletedUsage, grant, new BigDecimal("1.0000")
        )));
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
        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        )));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> result = deductionRepository.findByGrantIds(List.of(grant.getId(), 999L));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGrant().getId()).isEqualTo(grant.getId());
    }

    @Test
    @DisplayName("여러 사용 ID로 차감 내역 일괄 조회")
    void findByUsageIds() {
        // given
        VacationUsage usage2 = VacationUsage.createVacationUsage(
                user, "연차 사용2", VacationTimeType.MORNINGOFF,
                LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 13, 0),
                new BigDecimal("0.5000")
        );
        em.persist(usage2);

        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        )));
        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                usage2, grant, new BigDecimal("0.5000")
        )));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> result = deductionRepository.findByUsageIds(List.of(usage.getId(), usage2.getId()));

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(d -> d.getUsage().getId())
                .containsExactlyInAnyOrder(usage.getId(), usage2.getId());
    }

    @Test
    @DisplayName("여러 사용 ID로 조회 시 빈 리스트 전달하면 빈 결과 반환")
    void findByUsageIdsEmpty() {
        // when
        List<VacationUsageDeduction> result = deductionRepository.findByUsageIds(List.of());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 사용 ID로 조회 시 null 전달하면 빈 결과 반환")
    void findByUsageIdsNull() {
        // when
        List<VacationUsageDeduction> result = deductionRepository.findByUsageIds(null);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("여러 사용 ID로 조회 시 존재하지 않는 ID가 포함되어도 정상 조회")
    void findByUsageIdsWithNonExistentId() {
        // given
        deductionRepository.saveAll(List.of(VacationUsageDeduction.createVacationUsageDeduction(
                usage, grant, new BigDecimal("1.0000")
        )));
        em.flush();
        em.clear();

        // when
        List<VacationUsageDeduction> result = deductionRepository.findByUsageIds(List.of(usage.getId(), 999L));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUsage().getId()).isEqualTo(usage.getId());
    }
}
