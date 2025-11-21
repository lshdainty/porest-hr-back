package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationGrantCustomRepositoryImpl;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({VacationGrantCustomRepositoryImpl.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 휴가부여 레포지토리 테스트")
class VacationGrantRepositoryImplTest {
    @Autowired
    private VacationGrantCustomRepositoryImpl vacationGrantRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private VacationPolicy policy;

    @BeforeEach
    void setUp() {
        user = User.createUser("user1");
        em.persist(user);

        policy = VacationPolicy.createManualGrantPolicy(
                "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("8.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(policy);
    }

    @Test
    @DisplayName("휴가부여 저장 및 단건 조회")
    void save() {
        // given
        VacationGrant grant = VacationGrant.createVacationGrant(
                user, policy, "2025년 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        );

        // when
        vacationGrantRepository.save(grant);
        em.flush();
        em.clear();

        // then
        Optional<VacationGrant> findGrant = vacationGrantRepository.findById(grant.getId());
        assertThat(findGrant.isPresent()).isTrue();
        assertThat(findGrant.get().getDesc()).isEqualTo("2025년 연차");
        assertThat(findGrant.get().getGrantTime()).isEqualByComparingTo(new BigDecimal("8.0"));
    }

    @Test
    @DisplayName("단건 조회 시 휴가부여가 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<VacationGrant> findGrant = vacationGrantRepository.findById(999L);

        // then
        assertThat(findGrant.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저 ID로 휴가부여 목록 조회")
    void findByUserId() {
        // given
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차1", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차2", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findByUserId("user1");

        // then
        assertThat(grants).hasSize(2);
        assertThat(grants).extracting("desc").containsExactlyInAnyOrder("연차1", "연차2");
    }

    @Test
    @DisplayName("유저 ID로 조회 시 휴가부여가 없어도 Null이 반환되면 안된다.")
    void findByUserIdEmpty() {
        // given & when
        List<VacationGrant> grants = vacationGrantRepository.findByUserId("user1");

        // then
        assertThat(grants.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("정책 ID로 휴가부여 목록 조회")
    void findByPolicyId() {
        // given
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findByPolicyId(policy.getId());

        // then
        assertThat(grants).hasSize(1);
        assertThat(grants.get(0).getDesc()).isEqualTo("연차");
    }

    @Test
    @DisplayName("사용 가능한 휴가부여 만료일 순 조회")
    void findAvailableGrantsByUserIdOrderByExpiryDate() {
        // given
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차1", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 6, 30, 23, 59, 59)
        ));
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차2", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findAvailableGrantsByUserIdOrderByExpiryDate("user1");

        // then
        assertThat(grants).hasSize(2);
        assertThat(grants.get(0).getDesc()).isEqualTo("연차1");
    }

    @Test
    @DisplayName("만료된 휴가부여 조회")
    void findExpiredTargets() {
        // given
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "만료된 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2024, 1, 1, 0, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59, 59)
        ));
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "유효한 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> expiredGrants = vacationGrantRepository.findExpiredTargets(LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        // then
        assertThat(expiredGrants).hasSize(1);
        assertThat(expiredGrants.get(0).getDesc()).isEqualTo("만료된 연차");
    }

    @Test
    @DisplayName("전체 휴가부여와 유저 함께 조회")
    void findAllWithUser() {
        // given
        User user2 = User.createUser("user2");
        em.persist(user2);

        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차1", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user2, policy, "연차2", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findAllWithUser();

        // then
        assertThat(grants).hasSize(2);
    }

    @Test
    @DisplayName("휴가부여 배치 저장")
    void saveAll() {
        // given
        List<VacationGrant> grants = List.of(
                VacationGrant.createVacationGrant(user, policy, "연차1", VacationType.ANNUAL, new BigDecimal("8.0"),
                        LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)),
                VacationGrant.createVacationGrant(user, policy, "연차2", VacationType.ANNUAL, new BigDecimal("8.0"),
                        LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59))
        );

        // when
        vacationGrantRepository.saveAll(grants);
        em.flush();
        em.clear();

        // then
        List<VacationGrant> savedGrants = vacationGrantRepository.findByUserId("user1");
        assertThat(savedGrants).hasSize(2);
    }

    @Test
    @DisplayName("휴가부여 차감 및 복원")
    void deductAndRestore() {
        // given
        VacationGrant grant = VacationGrant.createVacationGrant(
                user, policy, "연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        );
        vacationGrantRepository.save(grant);
        em.flush();
        em.clear();

        // when - 차감
        VacationGrant foundGrant = vacationGrantRepository.findById(grant.getId()).orElseThrow();
        foundGrant.deduct(new BigDecimal("4.0"));
        em.flush();
        em.clear();

        // then - 차감 확인
        VacationGrant deductedGrant = vacationGrantRepository.findById(grant.getId()).orElseThrow();
        assertThat(deductedGrant.getRemainTime()).isEqualByComparingTo(new BigDecimal("4.0"));
    }
}
