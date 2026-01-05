package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationPlan;

import java.util.List;
import java.util.Optional;

/**
 * VacationPlan Repository Interface<br>
 * QueryDSL을 활용한 휴가 플랜 조회 인터페이스
 */
public interface VacationPlanRepository {
    /**
     * 신규 휴가 플랜 저장
     *
     * @param vacationPlan 저장할 휴가 플랜
     */
    void save(VacationPlan vacationPlan);

    /**
     * 휴가 플랜 ID로 조회 (정책 포함 - fetch join)
     *
     * @param id 플랜 ID
     * @return Optional<VacationPlan>
     */
    Optional<VacationPlan> findByIdWithPolicies(Long id);

    /**
     * 휴가 플랜 코드로 조회
     *
     * @param code 플랜 코드
     * @return Optional<VacationPlan>
     */
    Optional<VacationPlan> findByCode(String code);

    /**
     * 휴가 플랜 코드로 조회 (정책 포함 - fetch join)
     *
     * @param code 플랜 코드
     * @return Optional<VacationPlan>
     */
    Optional<VacationPlan> findByCodeWithPolicies(String code);

    /**
     * 전체 휴가 플랜 목록 조회 (정책 포함 - fetch join)
     *
     * @return List<VacationPlan>
     */
    List<VacationPlan> findAllWithPolicies();

    /**
     * 휴가 플랜 코드 존재 여부 확인
     *
     * @param code 플랜 코드
     * @return 존재 여부
     */
    boolean existsByCode(String code);
}
