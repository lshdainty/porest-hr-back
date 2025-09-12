package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.VacationHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VacationHistoryRepository {
    // 휴가 history 저장
    void save(VacationHistory vacationHistory);
    // 단건 휴가내역 조회(delete용)
    Optional<VacationHistory> findById(Long vacationHistoryId);
    // Calendar용 기간 휴가 내역 조회
    List<VacationHistory> findVacationHistorysByPeriod(LocalDateTime start, LocalDateTime end);
    // 유저 기간 휴가 내역 조회
    List<VacationHistory> findVacationUseHistorysByUserAndPeriod(String userId, LocalDateTime start, LocalDateTime end);
}
