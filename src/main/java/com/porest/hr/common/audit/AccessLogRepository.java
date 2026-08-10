package com.porest.hr.common.audit;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AccessLog Repository Interface
 *
 * <p>접속기록은 <strong>남기고 읽기만</strong> 한다. 수정·삭제 메소드를 두지 않는 것은
 * 고시 제8조 2항(위·변조 방지)에 따른 의도이며, DB 계정 권한으로도 함께 막는다.
 */
public interface AccessLogRepository {

    /**
     * 접속기록 저장
     *
     * @param accessLog 저장할 접속기록
     */
    void save(AccessLog accessLog);

    /**
     * 수행자별 접속기록 조회 (최신순)
     *
     * @param actorId 수행자 계정
     * @param limit 최대 건수
     * @return 접속기록 목록
     */
    List<AccessLog> findByActor(String actorId, int limit);

    /**
     * 정보주체별 접속기록 조회 (최신순)<br>
     * "내 정보를 누가 열람했는가" 를 확인할 때 사용
     *
     * @param targetType 대상 유형 (USER 등)
     * @param targetId 정보주체 식별자
     * @param limit 최대 건수
     * @return 접속기록 목록
     */
    List<AccessLog> findByTarget(String targetType, String targetId, int limit);

    /**
     * 기간별 접속기록 조회 (최신순)<br>
     * 정기 점검 시 사용
     *
     * @param from 시작 일시 [UTC]
     * @param to 종료 일시 [UTC]
     * @param limit 최대 건수
     * @return 접속기록 목록
     */
    List<AccessLog> findByPeriod(LocalDateTime from, LocalDateTime to, int limit);
}
