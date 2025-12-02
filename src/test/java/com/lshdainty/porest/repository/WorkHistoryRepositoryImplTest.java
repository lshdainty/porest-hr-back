package com.lshdainty.porest.repository;

import com.lshdainty.porest.user.domain.User;
import com.lshdainty.porest.work.domain.WorkCode;
import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;
import com.lshdainty.porest.work.repository.WorkHistoryQueryDslRepository;
import com.lshdainty.porest.work.type.CodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({ WorkHistoryQueryDslRepository.class, TestQuerydslConfig.class })
@Transactional
@DisplayName("JPA 업무이력 레포지토리 테스트")
class WorkHistoryRepositoryImplTest {
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

        group = WorkCode.createWorkCode("DEV", "개발", CodeType.LABEL, null, 1);
        em.persist(group);

        part = WorkCode.createWorkCode("BACKEND", "백엔드", CodeType.LABEL, group, 1);
        em.persist(part);

        division = WorkCode.createWorkCode("API", "API 개발", CodeType.OPTION, part, 1);
        em.persist(division);
    }

    @Test
    @DisplayName("업무이력 저장 및 단건 조회")
    void save() {
        // given
        WorkHistory workHistory = WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 15), user, group, part, division, new BigDecimal("8.0"), "API 개발 업무 진행");

        // when
        workHistoryRepository.save(workHistory);
        em.flush();
        em.clear();

        // then
        Optional<WorkHistory> findHistory = workHistoryRepository.findById(workHistory.getSeq());
        assertThat(findHistory.isPresent()).isTrue();
        assertThat(findHistory.get().getDate()).isEqualTo(LocalDate.of(2025, 1, 15));
        assertThat(findHistory.get().getContent()).isEqualTo("API 개발 업무 진행");
    }

    @Test
    @DisplayName("단건 조회 시 업무이력이 없어도 Null이 반환되면 안된다.")
    void findByIdEmpty() {
        // given & when
        Optional<WorkHistory> findHistory = workHistoryRepository.findById(999L);

        // then
        assertThat(findHistory.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("전체 업무이력 조회")
    void findAll() {
        // given
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 15), user, group, part, division, new BigDecimal("8.0"), "업무1"));
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 16), user, group, part, division, new BigDecimal("4.0"), "업무2"));
        em.flush();
        em.clear();

        // when
        List<WorkHistory> histories = workHistoryRepository.findAll(new WorkHistorySearchCondition());

        // then
        assertThat(histories).hasSize(2);
        assertThat(histories.get(0).getDate()).isEqualTo(LocalDate.of(2025, 1, 16));
    }

    @Test
    @DisplayName("전체 업무이력이 없어도 Null이 반환되면 안된다.")
    void findAllEmpty() {
        // given & when
        List<WorkHistory> histories = workHistoryRepository.findAll(new WorkHistorySearchCondition());

        // then
        assertThat(histories.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("업무이력 수정")
    void updateWorkHistory() {
        // given
        WorkHistory history = WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 15), user, group, part, division, new BigDecimal("8.0"), "기존 업무");
        workHistoryRepository.save(history);
        em.flush();
        em.clear();

        // when
        WorkHistory findHistory = workHistoryRepository.findById(history.getSeq()).orElseThrow();
        findHistory.updateWorkHistory(LocalDate.of(2025, 1, 16), null, null, null, null, new BigDecimal("4.0"),
                "수정된 업무");
        em.flush();
        em.clear();

        // then
        WorkHistory updatedHistory = workHistoryRepository.findById(history.getSeq()).orElseThrow();
        assertThat(updatedHistory.getDate()).isEqualTo(LocalDate.of(2025, 1, 16));
        assertThat(updatedHistory.getContent()).isEqualTo("수정된 업무");
    }

    @Test
    @DisplayName("여러 유저의 업무이력 조회")
    void findAllMultipleUsers() {
        // given
        User user2 = User.createUser("user2");
        em.persist(user2);

        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 15), user, group, part, division, new BigDecimal("8.0"), "user1 업무"));
        workHistoryRepository.save(WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 15), user2, group, part, division, new BigDecimal("8.0"), "user2 업무"));
        em.flush();
        em.clear();

        // when
        List<WorkHistory> histories = workHistoryRepository.findAll(new WorkHistorySearchCondition());

        // then
        assertThat(histories).hasSize(2);
    }

    @Test
    @DisplayName("업무이력 조회 시 연관 엔티티 함께 조회")
    void findByIdWithRelations() {
        // given
        WorkHistory history = WorkHistory.createWorkHistory(
                LocalDate.of(2025, 1, 15), user, group, part, division, new BigDecimal("8.0"), "업무");
        workHistoryRepository.save(history);
        em.flush();
        em.clear();

        // when
        Optional<WorkHistory> findHistory = workHistoryRepository.findById(history.getSeq());

        // then
        assertThat(findHistory.isPresent()).isTrue();
        assertThat(findHistory.get().getUser()).isNotNull();
        assertThat(findHistory.get().getGroup()).isNotNull();
    }
}
