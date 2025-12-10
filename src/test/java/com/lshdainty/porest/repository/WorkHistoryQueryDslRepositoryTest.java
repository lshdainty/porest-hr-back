package com.lshdainty.porest.repository;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.WorkHistoryQueryDslRepository;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.lshdainty.porest.work.type.CodeType;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({WorkHistoryQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 업무이력 레포지토리 테스트")
class WorkHistoryQueryDslRepositoryTest {
    @Autowired
    private WorkHistoryQueryDslRepository workHistoryRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private WorkCode group;
    private WorkCode part;
    private WorkCode division;

    @BeforeEach
    void setUp() {
        user = User.createUser("user1");
        em.persist(user);

        group = WorkCode.createWorkCode("GRP001", "개발팀", CodeType.LABEL, null, 1);
        em.persist(group);

        part = WorkCode.createWorkCode("PART001", "백엔드", CodeType.OPTION, group, 1);
        em.persist(part);

        division = WorkCode.createWorkCode("DIV001", "API개발", CodeType.OPTION, part, 1);
        em.persist(division);
    }

    @Test
    @DisplayName("업무이력 저장 및 단건 조회")
    void save() {
        // given
        WorkHistory workHistory = WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("8.0"), "API 개발"
        );

        // when
        workHistoryRepository.save(workHistory);
        em.flush();
        em.clear();

        // then
        Optional<WorkHistory> findHistory = workHistoryRepository.findById(workHistory.getId());
        assertThat(findHistory.isPresent()).isTrue();
        assertThat(findHistory.get().getContent()).isEqualTo("API 개발");
        assertThat(findHistory.get().getHours()).isEqualByComparingTo(new BigDecimal("8.0"));
    }

    @Test
    @DisplayName("단건 조회 시 업무이력이 없으면 빈 Optional 반환")
    void findByIdEmpty() {
        // when
        Optional<WorkHistory> findHistory = workHistoryRepository.findById(999L);

        // then
        assertThat(findHistory.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("업무이력 다건 저장")
    void saveAll() {
        // given
        List<WorkHistory> histories = List.of(
                WorkHistory.createWorkHistory(LocalDate.of(2025, 1, 1), user, group, part, division,
                        new BigDecimal("4.0"), "작업1"),
                WorkHistory.createWorkHistory(LocalDate.of(2025, 1, 1), user, group, part, division,
                        new BigDecimal("4.0"), "작업2")
        );

        // when
        workHistoryRepository.saveAll(histories);
        em.flush();
        em.clear();

        // then
        List<WorkHistory> result = workHistoryRepository.findByUserAndDate("user1", LocalDate.of(2025, 1, 1));
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("조건으로 전체 업무이력 조회")
    void findAll() {
        // given
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("8.0"), "작업"
        ));
        em.flush();
        em.clear();

        // when
        WorkHistorySearchCondition condition = new WorkHistorySearchCondition();
        condition.setUserId("user1");
        List<WorkHistory> histories = workHistoryRepository.findAll(condition);

        // then
        assertThat(histories).hasSize(1);
    }

    @Test
    @DisplayName("날짜 범위로 조회")
    void findAllWithDateRange() {
        // given
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 15), user, group, part, division,
                new BigDecimal("8.0"), "1월 작업"
        ));
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 2, 15), user, group, part, division,
                new BigDecimal("8.0"), "2월 작업"
        ));
        em.flush();
        em.clear();

        // when
        WorkHistorySearchCondition condition = new WorkHistorySearchCondition();
        condition.setStartDate(LocalDate.of(2025, 1, 1));
        condition.setEndDate(LocalDate.of(2025, 1, 31));
        List<WorkHistory> histories = workHistoryRepository.findAll(condition);

        // then
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getContent()).isEqualTo("1월 작업");
    }

    @Test
    @DisplayName("삭제된 업무이력 제외")
    void findAllExcludesDeleted() {
        // given
        WorkHistory activeHistory = WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("8.0"), "활성 작업"
        );
        WorkHistory deletedHistory = WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 2), user, group, part, division,
                new BigDecimal("8.0"), "삭제 작업"
        );
        workHistoryRepository.save(activeHistory);
        workHistoryRepository.save(deletedHistory);
        deletedHistory.deleteWorkHistory();
        em.flush();
        em.clear();

        // when
        WorkHistorySearchCondition condition = new WorkHistorySearchCondition();
        condition.setUserId("user1");
        List<WorkHistory> histories = workHistoryRepository.findAll(condition);

        // then
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getContent()).isEqualTo("활성 작업");
    }

    @Test
    @DisplayName("업무이력 삭제 (소프트 딜리트)")
    void delete() {
        // given
        WorkHistory workHistory = WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("8.0"), "삭제 대상"
        );
        workHistoryRepository.save(workHistory);
        em.flush();
        em.clear();

        // when
        WorkHistory foundHistory = workHistoryRepository.findById(workHistory.getId()).orElseThrow();
        workHistoryRepository.delete(foundHistory);
        em.flush();
        em.clear();

        // then
        WorkHistorySearchCondition condition = new WorkHistorySearchCondition();
        condition.setUserId("user1");
        List<WorkHistory> histories = workHistoryRepository.findAll(condition);
        assertThat(histories).isEmpty();
    }

    @Test
    @DisplayName("사용자와 날짜로 업무이력 조회")
    void findByUserAndDate() {
        // given
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("4.0"), "작업1"
        ));
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("4.0"), "작업2"
        ));
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 2), user, group, part, division,
                new BigDecimal("8.0"), "다른 날짜"
        ));
        em.flush();
        em.clear();

        // when
        List<WorkHistory> histories = workHistoryRepository.findByUserAndDate("user1", LocalDate.of(2025, 1, 1));

        // then
        assertThat(histories).hasSize(2);
    }

    @Test
    @DisplayName("기간별 일일 업무시간 합계 조회")
    void findDailyWorkHoursByUserAndPeriod() {
        // given
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("4.0"), "작업1"
        ));
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("4.0"), "작업2"
        ));
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 2), user, group, part, division,
                new BigDecimal("6.0"), "다른 날짜"
        ));
        em.flush();
        em.clear();

        // when
        Map<LocalDate, BigDecimal> dailyHours = workHistoryRepository.findDailyWorkHoursByUserAndPeriod(
                "user1", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
        );

        // then
        assertThat(dailyHours).hasSize(2);
        assertThat(dailyHours.get(LocalDate.of(2025, 1, 1))).isEqualByComparingTo(new BigDecimal("8.0"));
        assertThat(dailyHours.get(LocalDate.of(2025, 1, 2))).isEqualByComparingTo(new BigDecimal("6.0"));
    }

    @Test
    @DisplayName("여러 사용자의 기간별 일일 업무시간 합계 조회")
    void findDailyWorkHoursByUsersAndPeriod() {
        // given
        User user2 = User.createUser("user2");
        em.persist(user2);

        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("8.0"), "user1 작업"
        ));
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user2, group, part, division,
                new BigDecimal("6.0"), "user2 작업"
        ));
        em.flush();
        em.clear();

        // when
        Map<String, Map<LocalDate, BigDecimal>> result = workHistoryRepository.findDailyWorkHoursByUsersAndPeriod(
                List.of("user1", "user2"), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get("user1").get(LocalDate.of(2025, 1, 1))).isEqualByComparingTo(new BigDecimal("8.0"));
        assertThat(result.get("user2").get(LocalDate.of(2025, 1, 1))).isEqualByComparingTo(new BigDecimal("6.0"));
    }

    @Test
    @DisplayName("빈 사용자 목록으로 조회 시 빈 맵 반환")
    void findDailyWorkHoursByUsersAndPeriodEmpty() {
        // when
        Map<String, Map<LocalDate, BigDecimal>> result = workHistoryRepository.findDailyWorkHoursByUsersAndPeriod(
                List.of(), LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
        );

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("업무이력 수정")
    void updateWorkHistory() {
        // given
        WorkHistory workHistory = WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 1), user, group, part, division,
                new BigDecimal("8.0"), "원래 내용"
        );
        workHistoryRepository.save(workHistory);
        em.flush();
        em.clear();

        // when
        WorkHistory foundHistory = workHistoryRepository.findById(workHistory.getId()).orElseThrow();
        foundHistory.updateWorkHistory(LocalDate.of(2025, 1, 2), null, null, null, null,
                new BigDecimal("4.0"), "수정된 내용");
        em.flush();
        em.clear();

        // then
        WorkHistory updatedHistory = workHistoryRepository.findById(workHistory.getId()).orElseThrow();
        assertThat(updatedHistory.getDate()).isEqualTo(LocalDate.of(2025, 1, 2));
        assertThat(updatedHistory.getHours()).isEqualByComparingTo(new BigDecimal("4.0"));
        assertThat(updatedHistory.getContent()).isEqualTo("수정된 내용");
    }
}
