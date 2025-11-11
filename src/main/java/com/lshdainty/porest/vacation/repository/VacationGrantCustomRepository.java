package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationGrant;

import java.util.List;

public interface VacationGrantCustomRepository {
    /**
     * VacationGrant 저장
     */
    void save(VacationGrant vacationGrant);

    /**
     * VacationGrant 일괄 저장
     */
    void saveAll(List<VacationGrant> vacationGrants);

    /**
     * 유저 ID로 VacationGrant 조회
     */
    List<VacationGrant> findByUserId(String userId);

    /**
     * 휴가 정책 ID로 VacationGrant 조회
     */
    List<VacationGrant> findByPolicyId(Long policyId);

    /**
     * 유저의 사용 가능한 VacationGrant 조회 (FIFO용)
     * 만료일이 가까운 순서로 정렬하여 반환
     *
     * @param userId 유저 ID
     * @return 만료일 오름차순으로 정렬된 VacationGrant 리스트
     */
    List<VacationGrant> findAvailableGrantsByUserIdOrderByExpiryDate(String userId);

    /**
     * 유저의 사용 가능한 VacationGrant 조회 (FIFO용 - VacationType 필터링 + 날짜 범위 체크)
     * - VacationType 일치
     * - remainTime > 0
     * - 휴가 사용 시작일(usageStartDate)이 grantDate와 expiryDate 사이
     * - 만료일이 가까운 순서로 정렬
     *
     * @param userId 유저 ID
     * @param vacationType 휴가 타입
     * @param usageStartDate 사용자가 사용하려는 휴가 시작일
     * @return 만료일 오름차순으로 정렬된 VacationGrant 리스트
     */
    List<VacationGrant> findAvailableGrantsByUserIdAndTypeAndDate(String userId, com.lshdainty.porest.vacation.type.VacationType vacationType, java.time.LocalDateTime usageStartDate);

    /**
     * 모든 VacationGrant 조회 (User 정보 포함)
     * 모든 유저의 휴가 부여 내역을 조회
     *
     * @return 모든 VacationGrant 리스트
     */
    List<VacationGrant> findAllWithUser();

    /**
     * 유저의 사용 가능한 VacationGrant 조회 (날짜 기준, 모든 타입)
     * - remainTime > 0
     * - 휴가 사용 시작일(usageStartDate)이 grantDate와 expiryDate 사이
     * - 만료일이 가까운 순서로 정렬
     *
     * @param userId 유저 ID
     * @param usageStartDate 사용자가 사용하려는 휴가 시작일
     * @return 만료일 오름차순으로 정렬된 VacationGrant 리스트
     */
    List<VacationGrant> findAvailableGrantsByUserIdAndDate(String userId, java.time.LocalDateTime usageStartDate);

    /**
     * baseTime 기준으로 유효한 VacationGrant 조회
     * - grantDate <= baseTime
     * - expiryDate >= baseTime
     *
     * @param userId 유저 ID
     * @param baseTime 기준 시간
     * @return baseTime 기준 유효한 VacationGrant 리스트
     */
    List<VacationGrant> findValidGrantsByUserIdAndBaseTime(String userId, java.time.LocalDateTime baseTime);

    /**
     * 만료 대상 VacationGrant 조회
     * - status == ACTIVE
     * - expiryDate < 현재 날짜
     * - isDeleted == N
     *
     * @param currentDate 현재 날짜
     * @return 만료 대상 VacationGrant 리스트
     */
    List<VacationGrant> findExpiredTargets(java.time.LocalDateTime currentDate);

    /**
     * ID로 VacationGrant 조회
     *
     * @param id VacationGrant ID
     * @return VacationGrant Optional
     */
    java.util.Optional<VacationGrant> findById(Long id);

    /**
     * 사용자 ID로 ON_REQUEST 방식의 모든 VacationGrant 조회 (모든 상태 포함)
     * - 승인대기, 활성, 소진, 만료, 회수, 거부 등 모든 상태 포함
     * - 신청일시 최신순으로 정렬
     *
     * @param userId 사용자 ID
     * @return ON_REQUEST 방식의 모든 VacationGrant 리스트
     */
    List<VacationGrant> findAllRequestedVacationsByUserId(String userId);

    /**
     * VacationGrant ID 리스트로 VacationGrant 조회
     * - User, Policy와 fetch join
     * - 신청일시 최신순으로 정렬
     *
     * @param vacationGrantIds VacationGrant ID 리스트
     * @return VacationGrant 리스트
     */
    List<VacationGrant> findByIdsWithUserAndPolicy(List<Long> vacationGrantIds);
}