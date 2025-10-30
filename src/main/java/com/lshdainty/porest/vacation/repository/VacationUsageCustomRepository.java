package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationUsage;

import java.util.List;
import java.util.Optional;

public interface VacationUsageCustomRepository {
    /**
     * VacationUsage 저장
     */
    void save(VacationUsage vacationUsage);

    /**
     * VacationUsage 일괄 저장
     */
    void saveAll(List<VacationUsage> vacationUsages);

    /**
     * ID로 VacationUsage 조회
     */
    Optional<VacationUsage> findById(Long vacationUsageId);

    /**
     * 유저 ID로 VacationUsage 조회
     */
    List<VacationUsage> findByUserId(String userId);
}
