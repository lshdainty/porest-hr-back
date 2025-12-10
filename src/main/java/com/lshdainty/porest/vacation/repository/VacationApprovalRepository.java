package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationApproval;

import java.util.List;
import java.util.Optional;

public interface VacationApprovalRepository {
    /**
     * VacationApproval 저장
     */
    void save(VacationApproval vacationApproval);

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
     * 승인자 ID로 모든 승인 목록 조회 (상태 필터 옵션)
     *
     * @param approverId 승인자 ID
     * @return VacationGrant 리스트 (중복 제거)
     */
    List<Long> findAllVacationGrantIdsByApproverId(String approverId);

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
     * ID로 VacationApproval 조회
     *
     * @param id VacationApproval ID
     * @return VacationApproval Optional
     */
    Optional<VacationApproval> findById(Long id);

    /**
     * ID로 VacationApproval 조회 (VacationGrant, User 페치 조인)
     *
     * @param id VacationApproval ID
     * @return VacationApproval Optional
     */
    Optional<VacationApproval> findByIdWithVacationGrantAndUser(Long id);
}
