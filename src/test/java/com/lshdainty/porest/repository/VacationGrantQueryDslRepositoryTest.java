package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.company.type.OriginCompanyType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.VacationGrant;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.VacationGrantQueryDslRepository;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
import com.lshdainty.porest.vacation.type.GrantStatus;
import com.lshdainty.porest.vacation.type.VacationType;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({VacationGrantQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 휴가부여 레포지토리 테스트")
class VacationGrantQueryDslRepositoryTest {
    @Autowired
    private VacationGrantQueryDslRepository vacationGrantRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private VacationPolicy policy;

    @BeforeEach
    void setUp() {
        user = User.createUser(
                "user1", "password", "테스트유저1", "user1@test.com",
                LocalDate.of(1990, 1, 1), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
        User user2 = User.createUser(
                "user2", "password", "테스트유저2", "user2@test.com",
                LocalDate.of(1991, 2, 2), OriginCompanyType.DTOL, "9 ~ 6",
                YNType.N, null, null, CountryCode.KR
        );
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
    @DisplayName("휴가부여 차감")
    void deduct() {
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

    @Test
    @DisplayName("유저별 타입별 날짜별 사용가능한 휴가부여 조회")
    void findAvailableGrantsByUserIdAndTypeAndDate() {
        // given
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findAvailableGrantsByUserIdAndTypeAndDate(
                "user1", VacationType.ANNUAL, LocalDateTime.of(2025, 6, 1, 0, 0)
        );

        // then
        assertThat(grants).hasSize(1);
    }

    @Test
    @DisplayName("유저별 날짜별 사용가능한 휴가부여 조회")
    void findAvailableGrantsByUserIdAndDate() {
        // given
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findAvailableGrantsByUserIdAndDate(
                "user1", LocalDateTime.of(2025, 6, 1, 0, 0)
        );

        // then
        assertThat(grants).hasSize(1);
    }

    @Test
    @DisplayName("유저별 기준시간 기준 유효한 휴가부여 조회")
    void findValidGrantsByUserIdAndBaseTime() {
        // given
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findValidGrantsByUserIdAndBaseTime(
                "user1", LocalDateTime.of(2025, 6, 1, 0, 0)
        );

        // then
        assertThat(grants).hasSize(1);
    }

    @Test
    @DisplayName("유저별 기준시간 기준 유효한 휴가부여 조회 - EXHAUSTED, EXPIRED 포함, REVOKED 제외")
    void findValidGrantsByUserIdAndBaseTime_includesExhaustedAndExpired_excludesRevoked() {
        // given
        LocalDateTime grantDate = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        LocalDateTime expiryDate = LocalDateTime.of(2025, 12, 31, 23, 59, 59);
        LocalDateTime baseTime = LocalDateTime.of(2025, 6, 1, 0, 0);

        // ACTIVE 상태 휴가
        VacationGrant activeGrant = VacationGrant.createVacationGrant(
                user, policy, "활성 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                grantDate, expiryDate
        );
        vacationGrantRepository.save(activeGrant);

        // EXHAUSTED 상태 휴가 (잔여시간 전부 차감)
        VacationGrant exhaustedGrant = VacationGrant.createVacationGrant(
                user, policy, "소진 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                grantDate, expiryDate
        );
        exhaustedGrant.deduct(new BigDecimal("8.0"));
        vacationGrantRepository.save(exhaustedGrant);

        // EXPIRED 상태 휴가
        VacationGrant expiredGrant = VacationGrant.createVacationGrant(
                user, policy, "만료 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                grantDate, expiryDate
        );
        expiredGrant.expire();
        vacationGrantRepository.save(expiredGrant);

        // REVOKED 상태 휴가 (조회되지 않아야 함)
        VacationGrant revokedGrant = VacationGrant.createVacationGrant(
                user, policy, "회수 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                grantDate, expiryDate
        );
        revokedGrant.revoke();
        vacationGrantRepository.save(revokedGrant);

        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findValidGrantsByUserIdAndBaseTime(
                "user1", baseTime
        );

        // then
        assertThat(grants).hasSize(3);
        assertThat(grants).extracting(VacationGrant::getStatus)
                .containsExactlyInAnyOrder(GrantStatus.ACTIVE, GrantStatus.EXHAUSTED, GrantStatus.EXPIRED);
        assertThat(grants).extracting(VacationGrant::getStatus)
                .doesNotContain(GrantStatus.REVOKED);
    }

    @Test
    @DisplayName("유저별 신청 휴가 목록 조회")
    void findAllRequestedVacationsByUserId() {
        // given
        VacationPolicy onRequestPolicy = VacationPolicy.createOnRequestPolicy(
                "신청연차", "신청 정책", VacationType.ANNUAL, new BigDecimal("1.0"),
                YNType.N, YNType.N, 1, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(onRequestPolicy);

        VacationGrant pendingGrant = VacationGrant.createPendingVacationGrant(
                user, onRequestPolicy, "연차 신청", VacationType.ANNUAL, new BigDecimal("1.0"),
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0), "개인 사유"
        );
        em.persist(pendingGrant);
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findAllRequestedVacationsByUserId("user1");

        // then
        assertThat(grants).hasSize(1);
    }

    @Test
    @DisplayName("ID 목록으로 휴가부여 조회")
    void findByIdsWithUserAndPolicy() {
        // given
        VacationGrant grant1 = VacationGrant.createVacationGrant(
                user, policy, "연차1", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        );
        VacationGrant grant2 = VacationGrant.createVacationGrant(
                user, policy, "연차2", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        );
        vacationGrantRepository.save(grant1);
        vacationGrantRepository.save(grant2);
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findByIdsWithUserAndPolicy(
                List.of(grant1.getId(), grant2.getId())
        );

        // then
        assertThat(grants).hasSize(2);
    }

    @Test
    @DisplayName("빈 ID 목록으로 조회시 빈 리스트 반환")
    void findByIdsWithUserAndPolicyEmpty() {
        // when
        List<VacationGrant> grants = vacationGrantRepository.findByIdsWithUserAndPolicy(List.of());

        // then
        assertThat(grants).isEmpty();
    }

    @Test
    @DisplayName("기간 내 유효한 휴가부여 조회")
    void findByUserIdAndValidPeriod() {
        // given
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findByUserIdAndValidPeriod(
                "user1",
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(grants).hasSize(1);
    }

    @Test
    @DisplayName("상태와 기간으로 휴가부여 조회")
    void findByUserIdAndStatusesAndPeriod() {
        // given
        VacationPolicy onRequestPolicy = VacationPolicy.createOnRequestPolicy(
                "신청연차", "신청 정책", VacationType.ANNUAL, new BigDecimal("1.0"),
                YNType.N, YNType.N, 1, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(onRequestPolicy);

        VacationGrant pendingGrant = VacationGrant.createPendingVacationGrant(
                user, onRequestPolicy, "연차 신청", VacationType.ANNUAL, new BigDecimal("1.0"),
                LocalDateTime.of(2025, 6, 15, 9, 0), LocalDateTime.of(2025, 6, 15, 18, 0), "개인 사유"
        );
        em.persist(pendingGrant);
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findByUserIdAndStatusesAndPeriod(
                "user1",
                List.of(GrantStatus.PENDING),
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(grants).hasSize(1);
    }

    @Test
    @DisplayName("유저별 연도별 휴가부여 조회")
    void findByUserIdAndYear() {
        // given
        // 2025년 휴가
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "2025년 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2025, 1, 1, 0, 0, 0), LocalDateTime.of(2025, 12, 31, 23, 59, 59)
        ));
        // 2024년 휴가
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "2024년 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2024, 1, 1, 0, 0, 0), LocalDateTime.of(2024, 12, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findByUserIdAndYear("user1", 2025);

        // then
        assertThat(grants).hasSize(1);
        assertThat(grants.get(0).getDesc()).isEqualTo("2025년 연차");
    }

    @Test
    @DisplayName("유저별 연도별 휴가부여 조회 - 연도에 걸쳐있는 휴가도 포함")
    void findByUserIdAndYearWithCrossYear() {
        // given
        // 2024년~2025년에 걸쳐있는 휴가
        vacationGrantRepository.save(VacationGrant.createVacationGrant(
                user, policy, "연도걸침 연차", VacationType.ANNUAL, new BigDecimal("8.0"),
                LocalDateTime.of(2024, 6, 1, 0, 0, 0), LocalDateTime.of(2025, 5, 31, 23, 59, 59)
        ));
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findByUserIdAndYear("user1", 2025);

        // then
        assertThat(grants).hasSize(1);
    }

    @Test
    @DisplayName("유저별 연도별 신청 휴가 조회")
    void findAllRequestedVacationsByUserIdAndYear() {
        // given
        VacationPolicy onRequestPolicy = VacationPolicy.createOnRequestPolicy(
                "신청연차", "신청 정책", VacationType.ANNUAL, new BigDecimal("1.0"),
                YNType.N, YNType.N, 1, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(onRequestPolicy);

        VacationGrant pendingGrant = VacationGrant.createPendingVacationGrant(
                user, onRequestPolicy, "2025년 신청", VacationType.ANNUAL, new BigDecimal("1.0"),
                LocalDateTime.of(2025, 6, 15, 9, 0), LocalDateTime.of(2025, 6, 15, 18, 0), "개인 사유"
        );
        em.persist(pendingGrant);
        em.flush();
        em.clear();

        // when
        List<VacationGrant> grants = vacationGrantRepository.findAllRequestedVacationsByUserIdAndYear("user1", 2025);

        // then
        assertThat(grants).hasSize(1);
    }

    @Test
    @DisplayName("null ID 목록으로 조회시 빈 리스트 반환")
    void findByIdsWithUserAndPolicyNull() {
        // when
        List<VacationGrant> grants = vacationGrantRepository.findByIdsWithUserAndPolicy(null);

        // then
        assertThat(grants).isEmpty();
    }
}
