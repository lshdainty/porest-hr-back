package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.work.domain.QWorkSystemLog;
import com.lshdainty.porest.work.domain.WorkSystemLog;
import com.lshdainty.porest.work.type.SystemType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lshdainty.porest.work.domain.QWorkSystemLog.*;

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
    public Optional<WorkSystemLog> findTodayLogByCode(LocalDate today, SystemType code) {
        QWorkSystemLog workSystemLog = QWorkSystemLog.workSystemLog;

        // createDate가 오늘 날짜인지 확인 (시간 무시하고 날짜만 비교)
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        WorkSystemLog result = query
                .selectFrom(workSystemLog)
                .where(
                        workSystemLog.createDate.goe(startOfDay),
                        workSystemLog.createDate.lt(endOfDay),
                        workSystemLog.code.eq(code)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Map<SystemType, Boolean> findTodayLogsByCodes(LocalDate today, List<SystemType> codes) {
        QWorkSystemLog workSystemLog = QWorkSystemLog.workSystemLog;

        // createDate가 오늘 날짜인지 확인 (시간 무시하고 날짜만 비교)
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        // 오늘 날짜에 해당하는 코드들 조회
        List<SystemType> checkedCodes = query
                .select(workSystemLog.code)
                .from(workSystemLog)
                .where(
                        workSystemLog.createDate.goe(startOfDay),
                        workSystemLog.createDate.lt(endOfDay),
                        workSystemLog.code.in(codes)
                )
                .fetch();

        // 요청한 모든 코드에 대해 체크 여부를 Map으로 변환
        return codes.stream()
                .collect(Collectors.toMap(
                        code -> code,
                        code -> checkedCodes.contains(code)
                ));
    }

    @Override
    public void delete(WorkSystemLog log) {
        em.remove(log);
    }
}
