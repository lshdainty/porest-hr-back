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
}