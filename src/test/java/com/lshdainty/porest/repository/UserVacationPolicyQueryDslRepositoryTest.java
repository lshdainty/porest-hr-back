package com.lshdainty.porest.repository;

import com.lshdainty.porest.common.type.CountryCode;
import com.lshdainty.porest.common.type.YNType;
import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.UserVacationPolicy;
import com.lshdainty.porest.vacation.domain.VacationPolicy;
import com.lshdainty.porest.vacation.repository.UserVacationPolicyQueryDslRepository;
import com.lshdainty.porest.vacation.type.EffectiveType;
import com.lshdainty.porest.vacation.type.ExpirationType;
import com.lshdainty.porest.vacation.type.GrantMethod;
import com.lshdainty.porest.vacation.type.RepeatUnit;
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
@Import({UserVacationPolicyQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 유저휴가정책 레포지토리 테스트")
class UserVacationPolicyQueryDslRepositoryTest {
    @Autowired
    private UserVacationPolicyQueryDslRepository userVacationPolicyRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private VacationPolicy policy;

    @BeforeEach
    void setUp() {
        user = User.createUser("user1");
        em.persist(user);

        policy = VacationPolicy.createManualGrantPolicy(
                "연차", "연차 정책", VacationType.ANNUAL, new BigDecimal("15.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(policy);
    }

    @Test
    @DisplayName("유저휴가정책 저장")
    void save() {
        // given
        UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, policy);

        // when
        userVacationPolicyRepository.save(uvp);
        em.flush();
        em.clear();

        // then
        Optional<UserVacationPolicy> result = userVacationPolicyRepository.findById(uvp.getId());
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    @DisplayName("유저휴가정책 다건 저장")
    void saveAll() {
        // given
        VacationPolicy policy2 = VacationPolicy.createManualGrantPolicy(
                "경조", "경조 정책", VacationType.BEREAVEMENT, new BigDecimal("5.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.ONE_MONTHS_AFTER_GRANT
        );
        em.persist(policy2);

        List<UserVacationPolicy> uvps = List.of(
                UserVacationPolicy.createUserVacationPolicy(user, policy),
                UserVacationPolicy.createUserVacationPolicy(user, policy2)
        );

        // when
        userVacationPolicyRepository.saveAll(uvps);
        em.flush();
        em.clear();

        // then
        List<UserVacationPolicy> result = userVacationPolicyRepository.findByUserId("user1");
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("유저 ID로 유저휴가정책 조회")
    void findByUserId() {
        // given
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository.findByUserId("user1");

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("유저 ID로 조회 시 삭제된 정책 제외")
    void findByUserIdExcludesDeleted() {
        // given
        UserVacationPolicy activeUvp = UserVacationPolicy.createUserVacationPolicy(user, policy);

        VacationPolicy policy2 = VacationPolicy.createManualGrantPolicy(
                "건강검진", "건강검진 정책", VacationType.HEALTH, new BigDecimal("1.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(policy2);
        UserVacationPolicy deletedUvp = UserVacationPolicy.createUserVacationPolicy(user, policy2);

        userVacationPolicyRepository.save(activeUvp);
        userVacationPolicyRepository.save(deletedUvp);
        deletedUvp.deleteUserVacationPolicy();
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository.findByUserId("user1");

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("유저와 정책으로 존재 여부 확인 - 존재함")
    void existsByUserIdAndVacationPolicyIdTrue() {
        // given
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        em.flush();
        em.clear();

        // when
        boolean exists = userVacationPolicyRepository.existsByUserIdAndVacationPolicyId("user1", policy.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("유저와 정책으로 존재 여부 확인 - 존재하지 않음")
    void existsByUserIdAndVacationPolicyIdFalse() {
        // when
        boolean notExists = userVacationPolicyRepository.existsByUserIdAndVacationPolicyId("user1", 999L);

        // then
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("ID로 유저휴가정책 조회")
    void findById() {
        // given
        UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, policy);
        userVacationPolicyRepository.save(uvp);
        em.flush();
        em.clear();

        // when
        Optional<UserVacationPolicy> result = userVacationPolicyRepository.findById(uvp.getId());

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getVacationPolicy()).isNotNull();
        assertThat(result.get().getUser()).isNotNull();
    }

    @Test
    @DisplayName("ID로 조회 시 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<UserVacationPolicy> result = userVacationPolicyRepository.findById(999L);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("유저와 정책 ID로 유저휴가정책 조회")
    void findByUserIdAndVacationPolicyId() {
        // given
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        em.flush();
        em.clear();

        // when
        Optional<UserVacationPolicy> result = userVacationPolicyRepository
                .findByUserIdAndVacationPolicyId("user1", policy.getId());

        // then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getVacationPolicy()).isNotNull();
        assertThat(result.get().getUser()).isNotNull();
    }

    @Test
    @DisplayName("유저와 정책 ID로 조회 시 없으면 빈 Optional 반환")
    void findByUserIdAndVacationPolicyIdEmpty() {
        // when
        Optional<UserVacationPolicy> result = userVacationPolicyRepository
                .findByUserIdAndVacationPolicyId("user1", 999L);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("정책 ID로 유저휴가정책 목록 조회")
    void findByVacationPolicyId() {
        // given
        User user2 = User.createUser("user2");
        em.persist(user2);

        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user2, policy));
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository.findByVacationPolicyId(policy.getId());

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("정책 ID로 조회 시 없으면 빈 리스트 반환")
    void findByVacationPolicyIdEmpty() {
        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository.findByVacationPolicyId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("오늘 부여 대상 반복 정책 조회")
    void findRepeatGrantTargetsForToday() {
        // given
        VacationPolicy repeatPolicy = VacationPolicy.createRepeatGrantPolicy(
                "반복연차", "반복 정책", VacationType.ANNUAL,
                new BigDecimal("1.0"), YNType.N, RepeatUnit.YEARLY, 1, null, null,
                LocalDateTime.of(2025, 1, 1, 0, 0), YNType.Y, null,
                EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(repeatPolicy);

        UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, repeatPolicy);
        userVacationPolicyRepository.save(uvp);
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository
                .findRepeatGrantTargetsForToday(LocalDate.of(2025, 1, 1));

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("반복 정책 조회 시 MANUAL_GRANT 정책 제외")
    void findRepeatGrantTargetsForTodayExcludesManual() {
        // given
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository
                .findRepeatGrantTargetsForToday(LocalDate.now());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("반복 정책 조회 시 삭제된 정책 제외")
    void findRepeatGrantTargetsForTodayExcludesDeleted() {
        // given
        VacationPolicy repeatPolicy = VacationPolicy.createRepeatGrantPolicy(
                "반복연차", "반복 정책", VacationType.ANNUAL,
                new BigDecimal("1.0"), YNType.N, RepeatUnit.YEARLY, 1, null, null,
                LocalDateTime.of(2025, 1, 1, 0, 0), YNType.Y, null,
                EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(repeatPolicy);

        UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, repeatPolicy);
        userVacationPolicyRepository.save(uvp);
        uvp.deleteUserVacationPolicy();
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository
                .findRepeatGrantTargetsForToday(LocalDate.of(2025, 1, 1));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("필터 조건으로 유저휴가정책 조회 - 모든 필터")
    void findByUserIdWithFiltersAllFilters() {
        // given
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository
                .findByUserIdWithFilters("user1", VacationType.ANNUAL, GrantMethod.MANUAL_GRANT);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("필터 조건이 null이면 전체 조회")
    void findByUserIdWithFiltersNullFilter() {
        // given
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository
                .findByUserIdWithFilters("user1", null, null);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("휴가 타입 필터만 적용")
    void findByUserIdWithFiltersVacationTypeOnly() {
        // given
        VacationPolicy healthPolicy = VacationPolicy.createManualGrantPolicy(
                "건강검진", "건강검진 정책", VacationType.HEALTH, new BigDecimal("1.0"),
                YNType.N, YNType.N, EffectiveType.IMMEDIATELY, ExpirationType.END_OF_YEAR
        );
        em.persist(healthPolicy);

        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, healthPolicy));
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository
                .findByUserIdWithFilters("user1", VacationType.ANNUAL, null);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("부여 방식 필터만 적용")
    void findByUserIdWithFiltersGrantMethodOnly() {
        // given
        userVacationPolicyRepository.save(UserVacationPolicy.createUserVacationPolicy(user, policy));
        em.flush();
        em.clear();

        // when
        List<UserVacationPolicy> result = userVacationPolicyRepository
                .findByUserIdWithFilters("user1", null, GrantMethod.MANUAL_GRANT);

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("부여 이력 업데이트")
    void updateGrantHistory() {
        // given
        UserVacationPolicy uvp = UserVacationPolicy.createUserVacationPolicy(user, policy);
        userVacationPolicyRepository.save(uvp);
        em.flush();
        em.clear();

        // when
        UserVacationPolicy foundUvp = userVacationPolicyRepository.findById(uvp.getId()).orElseThrow();
        LocalDateTime lastGrantedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDate nextGrantDate = LocalDate.of(2026, 1, 1);
        foundUvp.updateGrantHistory(lastGrantedAt, nextGrantDate);
        em.flush();
        em.clear();

        // then
        UserVacationPolicy updatedUvp = userVacationPolicyRepository.findById(uvp.getId()).orElseThrow();
        assertThat(updatedUvp.getLastGrantedAt()).isEqualTo(lastGrantedAt);
        assertThat(updatedUvp.getNextGrantDate()).isEqualTo(nextGrantDate);
    }
}
