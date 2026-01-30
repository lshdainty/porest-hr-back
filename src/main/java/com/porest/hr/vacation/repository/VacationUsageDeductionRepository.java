package com.porest.hr.vacation.repository;

import com.porest.hr.vacation.domain.VacationUsageDeduction;

import java.util.List;

public interface VacationUsageDeductionRepository {
    /**
     * VacationUsageDeduction 일괄 저장
     */
    void saveAll(List<VacationUsageDeduction> deductions);

    /**
     * VacationUsage ID로 차감 내역 조회
     */
    List<VacationUsageDeduction> findByUsageId(Long usageId);

    /**
     * 여러 VacationGrant ID로 차감 내역 일괄 조회
     */
    List<VacationUsageDeduction> findByGrantIds(List<Long> grantIds);

    /**
     * 여러 VacationUsage ID로 차감 내역 일괄 조회
     */
    List<VacationUsageDeduction> findByUsageIds(List<Long> usageIds);
}