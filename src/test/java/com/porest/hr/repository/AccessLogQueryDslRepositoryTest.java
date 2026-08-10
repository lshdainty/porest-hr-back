package com.porest.hr.repository;

import com.porest.core.audit.AccessAction;
import com.porest.core.audit.AccessLogEntry;
import com.porest.hr.common.audit.AccessLog;
import com.porest.hr.common.audit.AccessLogQueryDslRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 개인정보 접속기록 레포지토리 테스트.
 *
 * <p>고시 제8조가 요구하는 5항목(계정·접속일시·접속지·처리한 정보주체·수행업무)이
 * 실제로 저장·조회되는지 확인한다.</p>
 */
@DataJpaTest
@Import({AccessLogQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 접속기록 레포지토리 테스트")
class AccessLogQueryDslRepositoryTest {

    @Autowired
    private AccessLogQueryDslRepository accessLogRepository;

    @Autowired
    private TestEntityManager em;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("고시가 요구하는 5항목이 그대로 저장된다")
    void savesAllRequiredFields() {
        AccessLogEntry entry = new AccessLogEntry(
                "admin", AccessAction.READ, "USER", "hong", "관리자 화면", "10.0.0.1", now);

        accessLogRepository.save(AccessLog.from(entry));
        em.flush();
        em.clear();

        List<AccessLog> found = accessLogRepository.findByActor("admin", 10);

        assertThat(found).hasSize(1);
        AccessLog saved = found.get(0);
        assertThat(saved.getActorId()).isEqualTo("admin");          // 계정
        assertThat(saved.getCreateAt()).isNotNull();                 // 접속일시
        assertThat(saved.getIpAddress()).isEqualTo("10.0.0.1");     // 접속지 정보
        assertThat(saved.getTargetType()).isEqualTo("USER");        // 처리한 정보주체
        assertThat(saved.getTargetId()).isEqualTo("hong");
        assertThat(saved.getAction()).isEqualTo("READ");             // 수행업무
        assertThat(saved.getDetail()).isEqualTo("관리자 화면");
    }

    @Test
    @DisplayName("목록 조회처럼 정보주체가 특정되지 않아도 기록은 남는다")
    void savesEvenWithoutTargetId() {
        accessLogRepository.save(AccessLog.from(
                new AccessLogEntry("admin", AccessAction.LIST, "USER", null, null, "10.0.0.1", now)));
        em.flush();
        em.clear();

        List<AccessLog> found = accessLogRepository.findByActor("admin", 10);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTargetId()).isNull();
        assertThat(found.get(0).getAction()).isEqualTo("LIST");
    }

    @Test
    @DisplayName("정보주체별 조회 — 내 정보를 누가 열람했는지 추적할 수 있다")
    void findsByTarget() {
        accessLogRepository.save(AccessLog.from(
                new AccessLogEntry("admin", AccessAction.READ, "USER", "hong", null, "10.0.0.1", now)));
        accessLogRepository.save(AccessLog.from(
                new AccessLogEntry("manager", AccessAction.UPDATE, "USER", "hong", null, "10.0.0.2", now)));
        accessLogRepository.save(AccessLog.from(
                new AccessLogEntry("admin", AccessAction.READ, "USER", "kim", null, "10.0.0.1", now)));
        em.flush();
        em.clear();

        List<AccessLog> found = accessLogRepository.findByTarget("USER", "hong", 10);

        assertThat(found).hasSize(2);
        assertThat(found).extracting(AccessLog::getActorId)
                .containsExactlyInAnyOrder("admin", "manager");
    }

    @Test
    @DisplayName("기간별 조회 — 정기 점검용")
    void findsByPeriod() {
        accessLogRepository.save(AccessLog.from(
                new AccessLogEntry("admin", AccessAction.READ, "USER", "hong", null, "10.0.0.1",
                        now.minusDays(10))));
        accessLogRepository.save(AccessLog.from(
                new AccessLogEntry("admin", AccessAction.READ, "USER", "kim", null, "10.0.0.1",
                        now.minusHours(1))));
        em.flush();
        em.clear();

        List<AccessLog> recent = accessLogRepository.findByPeriod(now.minusDays(1), now.plusDays(1), 10);

        assertThat(recent).hasSize(1);
        assertThat(recent.get(0).getTargetId()).isEqualTo("kim");
    }

    @Test
    @DisplayName("최신순으로 정렬된다")
    void ordersByCreateAtDesc() {
        accessLogRepository.save(AccessLog.from(
                new AccessLogEntry("admin", AccessAction.READ, "USER", "old", null, "10.0.0.1",
                        now.minusHours(2))));
        accessLogRepository.save(AccessLog.from(
                new AccessLogEntry("admin", AccessAction.READ, "USER", "new", null, "10.0.0.1", now)));
        em.flush();
        em.clear();

        List<AccessLog> found = accessLogRepository.findByActor("admin", 10);

        assertThat(found).extracting(AccessLog::getTargetId).containsExactly("new", "old");
    }
}
