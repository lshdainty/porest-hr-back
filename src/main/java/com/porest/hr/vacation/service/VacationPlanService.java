package com.porest.hr.vacation.service;

import com.porest.hr.vacation.service.dto.VacationPlanServiceDto;

import java.util.List;

/**
 * 휴가 플랜 서비스 인터페이스
 * 휴가 정책을 그룹화하여 관리하고 사용자에게 할당하는 기능을 제공
 */
public interface VacationPlanService {

    // ========================================
    // Plan CRUD
    // ========================================

    /**
     * 휴가 플랜 생성
     *
     * @param code 플랜 코드 (FULL_TIME, CONTRACT, INTERN 등)
     * @param name 플랜 이름
     * @param desc 플랜 설명
     * @return 생성된 휴가 플랜 정보
     */
    VacationPlanServiceDto createPlan(String code, String name, String desc);

    /**
     * 휴가 플랜 생성 (정책 포함)
     *
     * @param code 플랜 코드
     * @param name 플랜 이름
     * @param desc 플랜 설명
     * @param policyIds 포함할 휴가 정책 ID 목록
     * @return 생성된 휴가 플랜 정보
     */
    VacationPlanServiceDto createPlanWithPolicies(String code, String name, String desc, List<Long> policyIds);

    /**
     * 휴가 플랜 조회 (코드 기준)
     *
     * @param code 플랜 코드
     * @return 휴가 플랜 정보
     */
    VacationPlanServiceDto getPlan(String code);

    /**
     * 휴가 플랜 조회 (ID 기준)
     *
     * @param planId 플랜 ID
     * @return 휴가 플랜 정보
     */
    VacationPlanServiceDto getPlanById(Long planId);

    /**
     * 모든 휴가 플랜 조회
     *
     * @return 모든 휴가 플랜 목록
     */
    List<VacationPlanServiceDto> getAllPlans();

    /**
     * 휴가 플랜 수정
     *
     * @param code 플랜 코드
     * @param name 새로운 플랜 이름
     * @param desc 새로운 플랜 설명
     * @return 수정된 휴가 플랜 정보
     */
    VacationPlanServiceDto updatePlan(String code, String name, String desc);

    /**
     * 휴가 플랜 삭제 (소프트 삭제)
     *
     * @param code 플랜 코드
     */
    void deletePlan(String code);

    // ========================================
    // Plan-Policy 관리
    // ========================================

    /**
     * 플랜에 정책 추가
     *
     * @param planCode 플랜 코드
     * @param policyId 추가할 휴가 정책 ID
     */
    void addPolicyToPlan(String planCode, Long policyId);

    /**
     * 플랜에서 정책 제거
     *
     * @param planCode 플랜 코드
     * @param policyId 제거할 휴가 정책 ID
     */
    void removePolicyFromPlan(String planCode, Long policyId);

    /**
     * 플랜의 정책 목록 전체 업데이트
     *
     * @param planCode 플랜 코드
     * @param policyIds 새로운 휴가 정책 ID 목록
     */
    void updatePlanPolicies(String planCode, List<Long> policyIds);

    // ========================================
    // User-Plan 관리
    // ========================================

    /**
     * 사용자에게 플랜 할당
     * - 플랜 할당 시 해당 플랜의 REPEAT_GRANT 정책에 대한 VacationGrantSchedule 자동 생성
     *
     * @param userId 사용자 ID
     * @param planCode 할당할 플랜 코드
     */
    void assignPlanToUser(String userId, String planCode);

    /**
     * 사용자에게 여러 플랜 일괄 할당
     *
     * @param userId 사용자 ID
     * @param planCodes 할당할 플랜 코드 목록
     */
    void assignPlansToUser(String userId, List<String> planCodes);

    /**
     * 사용자에게서 플랜 회수
     * - 플랜 회수 시 해당 플랜의 정책에 대한 VacationGrantSchedule 삭제 (다른 플랜에 없는 경우만)
     *
     * @param userId 사용자 ID
     * @param planCode 회수할 플랜 코드
     */
    void revokePlanFromUser(String userId, String planCode);

    /**
     * 사용자에게 할당된 플랜 목록 조회
     *
     * @param userId 사용자 ID
     * @return 사용자에게 할당된 플랜 목록
     */
    List<VacationPlanServiceDto> getUserPlans(String userId);
}
