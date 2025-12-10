package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationUsageDeduction;

import java.util.List;

public interface VacationUsageDeductionRepository {
    /**
     * VacationUsageDeduction 저장
     */
    void save(VacationUsageDeduction deduction);

    /**
     * VacationUsageDeduction 일괄 저장
     */
    void saveAll(List<VacationUsageDeduction> deductions);

    /**
     * VacationUsage ID로 차감 내역 조회
     */
    List<VacationUsageDeduction> findByUsageId(Long usageId);

    /**
     * VacationGrant ID로 차감 내역 조회
     */
    List<VacationUsageDeduction> findByGrantId(Long grantId);

    /**
     * 여러 VacationGrant ID로 차감 내역 일괄 조회
     */
    List<VacationUsageDeduction> findByGrantIds(List<Long> grantIds);
}