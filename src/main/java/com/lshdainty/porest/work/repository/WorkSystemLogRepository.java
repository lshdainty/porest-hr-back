package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.work.domain.WorkSystemLog;
import com.lshdainty.porest.work.type.SystemType;

import java.time.LocalDate;
import java.util.Optional;

/**
 * WorkSystemLog Repository Interface<br>
 * 업무 시스템 로그 조회 인터페이스
 */
public interface WorkSystemLogRepository {
    /**
     * 시스템 로그 저장
     *
     * @param log 저장할 시스템 로그
     */
    void save(WorkSystemLog log);

    /**
     * 오늘 특정 시스템 코드 로그 조회<br>
     * createDate(오늘 날짜)와 code로 조회 (누가 체크했는지 무관)
     *
     * @param today 오늘 날짜
     * @param code 시스템 코드
     * @return Optional<WorkSystemLog>
     */
    Optional<WorkSystemLog> findTodayLogByCode(LocalDate today, SystemType code);

    /**
     * 시스템 로그 삭제
     *
     * @param log 삭제할 시스템 로그
     */
    void delete(WorkSystemLog log);
}
