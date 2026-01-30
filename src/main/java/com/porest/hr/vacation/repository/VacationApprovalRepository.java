package com.porest.hr.vacation.repository;

import com.porest.hr.vacation.domain.VacationApproval;

import java.util.List;
import java.util.Optional;

public interface VacationApprovalRepository {
    /**
     * VacationApproval 일괄 저장
     */
    void saveAll(List<VacationApproval> vacationApprovals);

    /**
     * VacationGrant ID로 VacationApproval 조회
     *
     * @param vacationGrantId VacationGrant ID
     * @return VacationApproval 리스트
     */
    List<VacationApproval> findByVacationGrantId(Long vacationGrantId);

    /**
     * 승인자 ID와 년도로 모든 승인 목록 조회
     * - VacationGrant의 createDate가 해당 년도에 해당하는 것만 조회
     *
     * @param approverId 승인자 ID
     * @param year 조회할 년도
     * @return VacationGrant ID 리스트 (중복 제거)
     */
    List<Long> findAllVacationGrantIdsByApproverIdAndYear(String approverId, Integer year);

    /**
     * ID로 VacationApproval 조회 (VacationGrant, User 페치 조인)
     *
     * @param id VacationApproval ID
     * @return VacationApproval Optional
     */
    Optional<VacationApproval> findByIdWithVacationGrantAndUser(Long id);

    /**
     * 여러 VacationGrant ID로 VacationApproval 일괄 조회
     *
     * @param vacationGrantIds VacationGrant ID 리스트
     * @return VacationApproval 리스트
     */
    List<VacationApproval> findByVacationGrantIds(List<Long> vacationGrantIds);
}
