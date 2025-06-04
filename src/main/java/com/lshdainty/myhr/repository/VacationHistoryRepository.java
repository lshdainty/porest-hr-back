package com.lshdainty.myhr.repository;

import com.lshdainty.myhr.domain.VacationHistory;

import java.util.Optional;

public interface VacationHistoryRepository {
    // 휴가 history 저장
    void save(VacationHistory vacationHistory);
    // 단건 휴가내역 조회(delete용)
    Optional<VacationHistory> findById(Long vacationHistoryId);
}
