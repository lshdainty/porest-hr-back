package com.lshdainty.porest.repository;

import com.lshdainty.porest.work.domain.WorkSystemLog;
import com.lshdainty.porest.work.repository.WorkSystemLogJpaRepository;
import com.lshdainty.porest.work.type.OriginSystemType;
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
@Import({WorkSystemLogJpaRepository.class, TestQuerydslConfig.class})
@Transactional
@DisplayName("JPA 업무시스템로그 레포지토리 테스트")
class WorkSystemLogJpaRepositoryTest {
    @Autowired
    private WorkSystemLogJpaRepository workSystemLogRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("시스템 로그 저장")
    void save() {
        // given
        WorkSystemLog log = WorkSystemLog.of(OriginSystemType.ERP);

        // when
        workSystemLogRepository.save(log);
        em.flush();
        em.clear();

        // then
        assertThat(log.getId()).isNotNull();
        assertThat(log.getCode()).isEqualTo(OriginSystemType.ERP);
    }

    @Test
    @DisplayName("기간과 코드로 시스템 로그 조회")
    void findByPeriodAndCode() {
        // given
        WorkSystemLog log = WorkSystemLog.of(OriginSystemType.ERP);
        workSystemLogRepository.save(log);
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        Optional<WorkSystemLog> findLog = workSystemLogRepository.findByPeriodAndCode(
                now.minusMinutes(1), now.plusMinutes(1), OriginSystemType.ERP
        );

        // then
        assertThat(findLog.isPresent()).isTrue();
        assertThat(findLog.get().getCode()).isEqualTo(OriginSystemType.ERP);
    }

    @Test
    @DisplayName("기간과 코드로 조회 시 로그가 없으면 빈 Optional 반환")
    void findByPeriodAndCodeEmpty() {
        // when
        Optional<WorkSystemLog> findLog = workSystemLogRepository.findByPeriodAndCode(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now(),
                OriginSystemType.MES
        );

        // then
        assertThat(findLog.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("기간과 코드로 조회 시 다른 코드는 제외")
    void findByPeriodAndCodeFiltersByCode() {
        // given
        workSystemLogRepository.save(WorkSystemLog.of(OriginSystemType.ERP));
        workSystemLogRepository.save(WorkSystemLog.of(OriginSystemType.MES));
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        Optional<WorkSystemLog> findLog = workSystemLogRepository.findByPeriodAndCode(
                now.minusMinutes(1), now.plusMinutes(1), OriginSystemType.ERP
        );

        // then
        assertThat(findLog.isPresent()).isTrue();
        assertThat(findLog.get().getCode()).isEqualTo(OriginSystemType.ERP);
    }

    @Test
    @DisplayName("기간과 여러 코드로 존재하는 코드 목록 조회")
    void findCodesByPeriodAndCodes() {
        // given
        workSystemLogRepository.save(WorkSystemLog.of(OriginSystemType.ERP));
        workSystemLogRepository.save(WorkSystemLog.of(OriginSystemType.MES));
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        List<OriginSystemType> codes = workSystemLogRepository.findCodesByPeriodAndCodes(
                now.minusMinutes(1), now.plusMinutes(1),
                List.of(OriginSystemType.ERP, OriginSystemType.MES, OriginSystemType.WMS)
        );

        // then
        assertThat(codes).hasSize(2);
        assertThat(codes).containsExactlyInAnyOrder(OriginSystemType.ERP, OriginSystemType.MES);
    }

    @Test
    @DisplayName("기간 외의 로그는 조회되지 않음")
    void findCodesByPeriodAndCodesOutOfRange() {
        // given
        workSystemLogRepository.save(WorkSystemLog.of(OriginSystemType.ERP));
        em.flush();
        em.clear();

        // when
        // 현재 시간보다 과거 기간으로 조회
        List<OriginSystemType> codes = workSystemLogRepository.findCodesByPeriodAndCodes(
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().minusDays(5),
                List.of(OriginSystemType.ERP)
        );

        // then
        assertThat(codes).isEmpty();
    }

    @Test
    @DisplayName("시스템 로그 삭제")
    void delete() {
        // given
        WorkSystemLog log = WorkSystemLog.of(OriginSystemType.ERP);
        workSystemLogRepository.save(log);
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        WorkSystemLog foundLog = workSystemLogRepository.findByPeriodAndCode(
                now.minusMinutes(1), now.plusMinutes(1), OriginSystemType.ERP
        ).orElseThrow();
        workSystemLogRepository.delete(foundLog);
        em.flush();
        em.clear();

        // then
        Optional<WorkSystemLog> deletedLog = workSystemLogRepository.findByPeriodAndCode(
                now.minusMinutes(1), now.plusMinutes(1), OriginSystemType.ERP
        );
        assertThat(deletedLog.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("여러 시스템 로그 저장 후 일부만 조회")
    void findCodesByPeriodAndCodesPartial() {
        // given
        workSystemLogRepository.save(WorkSystemLog.of(OriginSystemType.ERP));
        workSystemLogRepository.save(WorkSystemLog.of(OriginSystemType.CRM));
        em.flush();
        em.clear();

        // when
        LocalDateTime now = LocalDateTime.now();
        List<OriginSystemType> codes = workSystemLogRepository.findCodesByPeriodAndCodes(
                now.minusMinutes(1), now.plusMinutes(1),
                List.of(OriginSystemType.ERP, OriginSystemType.MES)  // MES는 저장 안함
        );

        // then
        assertThat(codes).hasSize(1);
        assertThat(codes).containsExactly(OriginSystemType.ERP);
    }
}
