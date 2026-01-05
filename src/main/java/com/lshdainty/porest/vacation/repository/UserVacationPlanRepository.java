package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.UserVacationPlan;

import java.util.List;
import java.util.Optional;

/**
 * UserVacationPlan Repository Interface<br>
 * QueryDSL을 활용한 사용자-플랜 매핑 조회 인터페이스
 */
public interface UserVacationPlanRepository {
    /**
     * 신규 사용자-플랜 매핑 저장
     *
     * @param userVacationPlan 저장할 사용자-플랜 매핑
     */
    void save(UserVacationPlan userVacationPlan);

    /**
     * 사용자 ID로 사용자-플랜 매핑 목록 조회 (플랜 및 정책 정보 포함 - fetch join)
     *
     * @param userId 사용자 ID
     * @return List<UserVacationPlan>
     */
    List<UserVacationPlan> findByUserIdWithPlanAndPolicies(String userId);

    /**
     * 사용자 ID와 플랜 코드로 사용자-플랜 매핑 조회
     *
     * @param userId 사용자 ID
     * @param planCode 플랜 코드
     * @return Optional<UserVacationPlan>
     */
    Optional<UserVacationPlan> findByUserIdAndPlanCode(String userId, String planCode);

    /**
     * 사용자 ID와 플랜 코드로 매핑 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @param planCode 플랜 코드
     * @return 존재 여부
     */
    boolean existsByUserIdAndPlanCode(String userId, String planCode);
}
