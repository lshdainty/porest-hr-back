package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.work.domain.WorkSystemLog;
import com.lshdainty.porest.work.type.SystemType;

import java.time.LocalDateTime;
import java.util.List;
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
     * 특정 기간 내 시스템 코드 로그 조회<br>
     * createDate가 startDateTime 이상, endDateTime 미만인 로그 조회
     *
     * @param startDateTime 조회 시작일시
     * @param endDateTime 조회 종료일시
     * @param code 시스템 코드
     * @return Optional<WorkSystemLog>
     */
    Optional<WorkSystemLog> findByPeriodAndCode(LocalDateTime startDateTime, LocalDateTime endDateTime, SystemType code);

    /**
     * 특정 기간 내 여러 시스템 코드의 로그를 배치 조회<br>
     * createDate가 startDateTime 이상, endDateTime 미만인 로그 조회
     *
     * @param startDateTime 조회 시작일시
     * @param endDateTime 조회 종료일시
     * @param codes 시스템 코드 목록
     * @return 해당 기간에 존재하는 시스템 코드 목록
     */
    List<SystemType> findCodesByPeriodAndCodes(LocalDateTime startDateTime, LocalDateTime endDateTime, List<SystemType> codes);

    /**
     * 시스템 로그 삭제
     *
     * @param log 삭제할 시스템 로그
     */
    void delete(WorkSystemLog log);
}
