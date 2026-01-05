package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationGrantSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * VacationGrantSchedule Repository Interface<br>
 * QueryDSL을 활용한 휴가 부여 스케줄 조회 인터페이스
 */
public interface VacationGrantScheduleRepository {
    /**
     * 신규 스케줄 저장
     *
     * @param schedule 저장할 스케줄
     */
    void save(VacationGrantSchedule schedule);

    /**
     * 사용자 ID와 정책 ID로 스케줄 조회
     *
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     * @return Optional<VacationGrantSchedule>
     */
    Optional<VacationGrantSchedule> findByUserIdAndPolicyId(String userId, Long policyId);

    /**
     * 사용자 ID와 정책 ID로 스케줄 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @param policyId 정책 ID
     * @return 존재 여부
     */
    boolean existsByUserIdAndPolicyId(String userId, Long policyId);

    /**
     * 오늘 부여 대상 스케줄 목록 조회<br>
     * 스케줄러에서 사용<br>
     * 조건: REPEAT_GRANT 정책, nextGrantDate <= today 또는 nextGrantDate is null
     *
     * @param today 오늘 날짜
     * @return List<VacationGrantSchedule>
     */
    List<VacationGrantSchedule> findRepeatGrantTargetsForToday(LocalDate today);
}
