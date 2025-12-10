package com.lshdainty.porest.repository;

import com.lshdainty.porest.work.domain.WorkSystemLog;
import com.lshdainty.porest.work.repository.WorkSystemLogQueryDslRepository;
import com.lshdainty.porest.work.type.SystemType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({WorkSystemLogQueryDslRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("QueryDSL 업무시스템로그 레포지토리 테스트")
class WorkSystemLogQueryDslRepositoryTest {
    @Autowired
    private WorkSystemLogQueryDslRepository workSystemLogRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("시스템 로그 저장")
    void save() {
        // given
        WorkSystemLog log = WorkSystemLog.of(SystemType.ERP);

        // when
        workSystemLogRepository.save(log);
        em.flush();
        em.clear();

        // then
        assertThat(log.getId()).isNotNull();
        assertThat(log.getCode()).isEqualTo(SystemType.ERP);
    }

    @Test
    @DisplayName("기간과 코드로 시스템 로그 조회")
    void findByPeriodAndCode() {
        // given
        WorkSystemLog log = WorkSystemLog.of(SystemType.ERP);
        workSystemLogRepository.save(log);
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        Optional<WorkSystemLog> findLog = workSystemLogRepository.findByPeriodAndCode(
                now.minusMinutes(1), now.plusMinutes(1), SystemType.ERP
        );

        // then
        assertThat(findLog.isPresent()).isTrue();
        assertThat(findLog.get().getCode()).isEqualTo(SystemType.ERP);
    }

    @Test
    @DisplayName("기간과 코드로 조회 시 로그가 없으면 빈 Optional 반환")
    void findByPeriodAndCodeEmpty() {
        // when
        Optional<WorkSystemLog> findLog = workSystemLogRepository.findByPeriodAndCode(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                SystemType.MES
        );

        // then
        assertThat(findLog.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("기간과 코드로 조회 시 다른 코드는 제외")
    void findByPeriodAndCodeFiltersByCode() {
        // given
        workSystemLogRepository.save(WorkSystemLog.of(SystemType.ERP));
        workSystemLogRepository.save(WorkSystemLog.of(SystemType.MES));
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        Optional<WorkSystemLog> findLog = workSystemLogRepository.findByPeriodAndCode(
                now.minusMinutes(1), now.plusMinutes(1), SystemType.ERP
        );

        // then
        assertThat(findLog.isPresent()).isTrue();
        assertThat(findLog.get().getCode()).isEqualTo(SystemType.ERP);
    }

    @Test
    @DisplayName("기간과 여러 코드로 존재하는 코드 목록 조회")
    void findCodesByPeriodAndCodes() {
        // given
        workSystemLogRepository.save(WorkSystemLog.of(SystemType.ERP));
        workSystemLogRepository.save(WorkSystemLog.of(SystemType.MES));
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        List<SystemType> codes = workSystemLogRepository.findCodesByPeriodAndCodes(
                now.minusMinutes(1), now.plusMinutes(1),
                List.of(SystemType.ERP, SystemType.MES, SystemType.WMS)
        );

        // then
        assertThat(codes).hasSize(2);
        assertThat(codes).containsExactlyInAnyOrder(SystemType.ERP, SystemType.MES);
    }

    @Test
    @DisplayName("기간 외의 로그는 조회되지 않음")
    void findCodesByPeriodAndCodesOutOfRange() {
        // given
        workSystemLogRepository.save(WorkSystemLog.of(SystemType.ERP));
        em.flush();
        em.clear();

        // when
        // 현재 시간보다 과거 기간으로 조회
        List<SystemType> codes = workSystemLogRepository.findCodesByPeriodAndCodes(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(5),
                List.of(SystemType.ERP)
        );

        // then
        assertThat(codes).isEmpty();
    }

    @Test
    @DisplayName("시스템 로그 삭제")
    void delete() {
        // given
        WorkSystemLog log = WorkSystemLog.of(SystemType.ERP);
        workSystemLogRepository.save(log);
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        WorkSystemLog foundLog = workSystemLogRepository.findByPeriodAndCode(
                now.minusMinutes(1), now.plusMinutes(1), SystemType.ERP
        ).orElseThrow();
        workSystemLogRepository.delete(foundLog);
        em.flush();
        em.clear();

        // then
        Optional<WorkSystemLog> deletedLog = workSystemLogRepository.findByPeriodAndCode(
                now.minusMinutes(1), now.plusMinutes(1), SystemType.ERP
        );
        assertThat(deletedLog.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("여러 시스템 로그 저장 후 일부만 조회")
    void findCodesByPeriodAndCodesPartial() {
        // given
        workSystemLogRepository.save(WorkSystemLog.of(SystemType.ERP));
        workSystemLogRepository.save(WorkSystemLog.of(SystemType.CRM));
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        List<SystemType> codes = workSystemLogRepository.findCodesByPeriodAndCodes(
                now.minusMinutes(1), now.plusMinutes(1),
                List.of(SystemType.ERP, SystemType.MES)  // MES는 저장 안함
        );

        // then
        assertThat(codes).hasSize(1);
        assertThat(codes).containsExactly(SystemType.ERP);
    }
}
