package com.lshdainty.porest.work.service;

import com.lshdainty.porest.work.type.SystemType;

import java.util.List;
import java.util.Map;

/**
 * 업무 시스템 로그 서비스<br>
 * 시스템 체크 관련 비즈니스 로직 처리<br>
 * - 사용자 정보는 AuditingFields의 createBy에서 자동으로 설정됨<br>
 * - 체크 시간은 AuditingFields의 createDate에서 자동으로 설정됨<br>
 * - 누가 체크했는지는 중요하지 않고, 시스템이 오늘 체크됐는지만 확인
 */
public interface WorkSystemLogService {

    /**
     * 시스템 체크 토글<br>
     * 오늘 날짜로 이미 체크된 로그가 있으면 삭제, 없으면 생성<br>
     * 누가 체크했는지는 무관하게 시스템 전체 체크 여부만 관리
     *
     * @param code 시스템 코드
     * @return true: 생성됨, false: 삭제됨
     */
    boolean toggleSystemCheck(SystemType code);

    /**
     * 오늘 날짜 특정 시스템 체크 여부 확인<br>
     * 누가 체크했는지는 무관
     *
     * @param code 시스템 코드
     * @return true: 체크됨, false: 체크 안됨
     */
    boolean isCheckedToday(SystemType code);

    /**
     * 오늘 날짜 여러 시스템의 체크 여부를 배치 확인<br>
     * 누가 체크했는지는 무관
     *
     * @param codes 시스템 코드 목록
     * @return Map<SystemType, Boolean> - 시스템 코드별 체크 여부
     */
    Map<SystemType, Boolean> checkSystemStatusBatch(List<SystemType> codes);
}
