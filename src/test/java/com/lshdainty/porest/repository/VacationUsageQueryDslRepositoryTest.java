package com.lshdainty.porest.repository;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.vacation.domain.VacationUsage;
import com.lshdainty.porest.vacation.repository.VacationUsageQueryDslRepository;
import com.lshdainty.porest.vacation.type.VacationTimeType;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({VacationUsageQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 휴가사용 레포지토리 테스트")
class VacationUsageQueryDslRepositoryTest {
    @Autowired
    private VacationUsageQueryDslRepository vacationUsageRepository;

    @Autowired
    private TestEntityManager em;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.createUser("user1");
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
        Optional<VacationUsage> findUsage = vacationUsageRepository.findById(usage.getId());
        assertThat(findUsage.isPresent()).isTrue();
        assertThat(findUsage.get().getDesc()).isEqualTo("연차");
    }

    @Test
    @DisplayName("휴가사용 다건 저장")
    void saveAll() {
        // given
        List<VacationUsage> usages = List.of(
                VacationUsage.createVacationUsage(user, "연차1", VacationTimeType.DAYOFF,
                        LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                        new BigDecimal("1.0000")),
                VacationUsage.createVacationUsage(user, "연차2", VacationTimeType.DAYOFF,
                        LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 18, 0),
                        new BigDecimal("1.0000"))
        );

        // when
        vacationUsageRepository.saveAll(usages);
        em.flush();
        em.clear();

        // then
        List<VacationUsage> result = vacationUsageRepository.findByUserId("user1");
        assertThat(result).hasSize(2);
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
    @DisplayName("유저 ID로 휴가사용 목록 조회")
    void findByUserId() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserId("user1");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("연차");
    }

    @Test
    @DisplayName("유저 ID로 조회 시 삭제된 사용 내역 제외")
    void findByUserIdExcludesDeleted() {
        // given
        VacationUsage activeUsage = VacationUsage.createVacationUsage(
                user, "활성 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 1, 9, 0), LocalDateTime.of(2025, 6, 1, 18, 0),
                new BigDecimal("1.0000")
        );
        VacationUsage deletedUsage = VacationUsage.createVacationUsage(
                user, "삭제 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 2, 9, 0), LocalDateTime.of(2025, 6, 2, 18, 0),
                new BigDecimal("1.0000")
        );
        vacationUsageRepository.save(activeUsage);
        vacationUsageRepository.save(deletedUsage);
        deletedUsage.deleteVacationUsage();
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserId("user1");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("활성 연차");
    }

    @Test
    @DisplayName("모든 휴가사용 목록 조회")
    void findAllWithUser() {
        // given
        User user2 = User.createUser("user2");
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
    @DisplayName("기준시간 이전 사용한 휴가 조회")
    void findUsedByUserIdAndBaseTime() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "과거연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 5, 1, 9, 0), LocalDateTime.of(2025, 5, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "미래연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 7, 1, 9, 0), LocalDateTime.of(2025, 7, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findUsedByUserIdAndBaseTime(
                "user1", LocalDateTime.of(2025, 6, 1, 0, 0)
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("과거연차");
    }

    @Test
    @DisplayName("기준시간 이후 예정된 휴가 조회")
    void findExpectedByUserIdAndBaseTime() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "과거연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 5, 1, 9, 0), LocalDateTime.of(2025, 5, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "미래연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 7, 1, 9, 0), LocalDateTime.of(2025, 7, 1, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findExpectedByUserIdAndBaseTime(
                "user1", LocalDateTime.of(2025, 6, 1, 0, 0)
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDesc()).isEqualTo("미래연차");
    }

    @Test
    @DisplayName("유저와 기간으로 휴가사용 조회 (between)")
    void findByUserIdAndPeriod() {
        // given
        vacationUsageRepository.save(VacationUsage.createVacationUsage(
                user, "6월 연차", VacationTimeType.DAYOFF,
                LocalDateTime.of(2025, 6, 15, 9, 0), LocalDateTime.of(2025, 6, 15, 18, 0),
                new BigDecimal("1.0000")
        ));
        em.flush();
        em.clear();

        // when
        List<VacationUsage> result = vacationUsageRepository.findByUserIdAndPeriod(
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
        User user2 = User.createUser("user2");
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
}
