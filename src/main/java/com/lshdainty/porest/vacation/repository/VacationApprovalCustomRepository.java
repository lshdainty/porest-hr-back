package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationApproval;

import java.util.List;
import java.util.Optional;

public interface VacationApprovalCustomRepository {
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
     * 승인자 ID로 대기 중인 승인 목록 조회
     *
     * @param approverId 승인자 ID
     * @return 대기 중인 VacationApproval 리스트
     */
    List<VacationApproval> findPendingApprovalsByApproverId(String approverId);

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
