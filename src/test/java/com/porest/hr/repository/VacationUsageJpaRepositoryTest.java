package com.porest.hr.repository;

import com.porest.core.type.CountryCode;
import com.porest.core.type.YNType;
import com.porest.hr.common.type.DefaultCompanyType;
import com.porest.hr.user.domain.User;
import com.porest.hr.vacation.domain.VacationUsage;
import com.porest.hr.vacation.repository.VacationUsageJpaRepository;
import com.porest.hr.vacation.type.VacationTimeType;
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
@Import({VacationUsageJpaRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 휴가사용 레포지토리 테스트")
class VacationUsageJpaRepositoryTest {
    @Autowired
    private VacationUsageJpaRepository vacationUsageRepository;

    @Autowired
    private TestEntityManager em;

    private User user;

    // 테스트용 User 생성 헬퍼 메소드
    private User createTestUser(String id, String name, String email) {
        return User.createUser(
                null, id, name, email,
                LocalDate.of(1990, 1, 1), DefaultCompanyType.NONE, "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
    }

    // 테스트용 User 생성 헬퍼 메소드 (생년월일 지정)
    private User createTestUser(String id, String name, String email, LocalDate birth) {
        return User.createUser(
                null, id, name, email,
                birth, DefaultCompanyType.NONE, "9 ~ 18",
                LocalDate.now(), YNType.N, null, null, CountryCode.KR
        );
    }

    @BeforeEach
    void setUp() {
        user = createTestUser("user1", "테스트유저1", "user1@test.com");
        em.persist(user);
    }

    @Test
    @DisplayName("휴가사용 저장 및 단건 조회")
    void save() {
        // given
        VacationUsage usage = VacationUsage.createVacationUsage(
                user, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        );

        // when
        vacationUsageRepository.save(usage);
        em.flush();
        em.clear();

        // then
        Optional<VacationUsage> findUsage = vacationUsageRepository.findById(usage.getRowId());
        assertThat(findUsage.isPresent()).isTrue();
        assertThat(findUsage.get().getDesc()).isEqualTo("연차");
    }

    @Test
    @DisplayName("단건 조회 시 휴가사용이 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<VacationUsage> result = vacationUsageRepository.findById(999L);

        // then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("모든 휴가사용 목록 조회")
    void findAllWithUser() {
        // given
        User user2 = createTestUser("user2", "테스트유저2", "user2@test.com", LocalDate.of(1991, 2, 2));
        em.persist(user2);

        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "연차1", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user2, "연차2", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findAllWithUser();

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("모든 휴가사용 목록 조회 시 삭제된 내역 제외")
    void findAllWithUserExcludesDeleted() {
        // given
        VacationUsage activeUsage = VacationUsage.createVacationUsage(
                user, "활성", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        );
        VacationUsage deletedUsage = VacationUsage.createVacationUsage(
                user, "삭제", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 18, 0),
                new BigDecimal("1.0000")
        );
        vacationUsageRepository.save(activeUsage);
        vacationUsageRepository.save(deletedUsage);
        deletedUsage.deleteVacationUsage();
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findAllWithUser();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("활성");
    }

    @Test
    @DisplayName("기간별 휴가사용 조회")
    void findByPeriodWithUser() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 15, 9, 0), LocalDateTime.of(2025, 6, 15, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByPeriodWithUser(
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("기간별 휴가사용 조회 시 기간 외 제외")
    void findByPeriodWithUserOutOfRange() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "5월 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 5, 15, 9, 0), LocalDateTime.of(2025, 5, 15, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByPeriodWithUser(
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("유저별 기간별 휴가사용 조회")
    void findByUserIdAndPeriodWithUser() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 15, 9, 0), LocalDateTime.of(2025, 6, 15, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdAndPeriodWithUser(
                "user1",
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("일별 휴가사용 조회 (goe, lt)")
    void findByUserIdAndPeriodForDaily() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "6월 1일 오전반차", VacationTimeType.MORNINGOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 13, 0),
                new BigDecimal("0.5000")
        ));
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "6월 2일 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdAndPeriodForDaily(
                "user1",
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 2, 0, 0)
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("6월 1일 오전반차");
    }

    @Test
    @DisplayName("여러 유저의 일별 휴가사용 조회")
    void findByUserIdsAndPeriodForDaily() {
        // given
        User user2 = createTestUser("user2", "테스트유저2", "user2@test.com", LocalDate.of(1991, 2, 2));
        em.persist(user2);

        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "user1 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user2, "user2 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdsAndPeriodForDaily(
                List.of("user1", "user2"),
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 2, 0, 0)
        );

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("빈 유저 목록으로 조회 시 빈 리스트 반환")
    void findByUserIdsAndPeriodForDailyEmpty() {
        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdsAndPeriodForDaily(
                List.of(),
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 2, 0, 0)
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null 유저 목록으로 조회 시 빈 리스트 반환")
    void findByUserIdsAndPeriodForDailyNull() {
        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdsAndPeriodForDaily(
                null,
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 2, 0, 0)
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("유저별 연도별 휴가사용 조회")
    void findByUserIdAndYear() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "2025년 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "2024년 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2024, 6, 1, 9, 0), LocalDateTime.of(2024, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdAndYear("user1", 2025);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("2025년 연차");
    }

    @Test
    @DisplayName("유저별 연도별 휴가사용 조회 - 삭제된 내역 제외")
    void findByUserIdAndYearExcludesDeleted() {
        // given
        VacationUsage activeUsage = VacationUsage.createVacationUsage(
                user, "활성 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        );
        VacationUsage deletedUsage = VacationUsage.createVacationUsage(
                user, "삭제 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 7, 1, 9, 0), LocalDateTime.of(2025, 7, 1, 18, 0),
                new BigDecimal("1.0000")
        );
        vacationUsageRepository.save(activeUsage);
        vacationUsageRepository.save(deletedUsage);
        deletedUsage.deleteVacationUsage();
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdAndYear("user1", 2025);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("활성 연차");
    }

    @Test
    @DisplayName("여러 유저의 기간 내 휴가사용 일괄 조회")
    void findByUserIdsAndPeriod() {
        // given
        User user2 = createTestUser("user2", "테스트유저2", "user2@test.com", LocalDate.of(1991, 2, 2));
        em.persist(user2);

        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "user1 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 15, 9, 0), LocalDateTime.of(2025, 6, 15, 18, 0),
                new BigDecimal("1.0000")
        ));
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user2, "user2 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 20, 9, 0), LocalDateTime.of(2025, 6, 20, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdsAndPeriod(
                List.of("user1", "user2"),
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("빈 유저 ID 목록으로 기간 내 휴가사용 조회 시 빈 리스트 반환")
    void findByUserIdsAndPeriodEmpty() {
        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdsAndPeriod(
                List.of(),
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null 유저 ID 목록으로 기간 내 휴가사용 조회 시 빈 리스트 반환")
    void findByUserIdsAndPeriodNull() {
        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdsAndPeriod(
                null,
                LocalDateTime.of(2025, 6, 1, 0, 0),
                LocalDateTime.of(2025, 6, 30, 23, 59)
        );

        // then
        assertThat(result).isEmpty();
    }
}
