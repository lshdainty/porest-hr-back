package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.work.domain.QWorkSystemLog;
import com.lshdainty.porest.work.domain.WorkSystemLog;
import com.lshdainty.porest.work.type.SystemType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * WorkSystemLog Repository 구현체<br>
 * QueryDSL을 활용한 업무 시스템 로그 조회 구현
 */
@Repository
@Primary
@RequiredArgsConstructor
public class WorkSystemLogQueryDslRepository implements WorkSystemLogRepository {
    private final EntityManager em;
    private final JPAQueryFactory query;

    @Override
    public void save(WorkSystemLog log) {
        em.persist(log);
    }

    @Override
    public Optional<WorkSystemLog> findByPeriodAndCode(LocalDateTime startDateTime, LocalDateTime endDateTime, SystemType code) {
        QWorkSystemLog workSystemLog = QWorkSystemLog.workSystemLog;

        WorkSystemLog result = query
                .selectFrom(workSystemLog)
                .where(
                        workSystemLog.createDate.goe(startDateTime),
                        workSystemLog.createDate.lt(endDateTime),
                        workSystemLog.code.eq(code)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<SystemType> findCodesByPeriodAndCodes(LocalDateTime startDateTime, LocalDateTime endDateTime, List<SystemType> codes) {
        QWorkSystemLog workSystemLog = QWorkSystemLog.workSystemLog;

        return query
                .select(workSystemLog.code)
                .from(workSystemLog)
                .where(
                        workSystemLog.createDate.goe(startDateTime),
                        workSystemLog.createDate.lt(endDateTime),
                        workSystemLog.code.in(codes)
                )
                .fetch();
    }

    @Override
    public void delete(WorkSystemLog log) {
        em.remove(log);
    }
}
