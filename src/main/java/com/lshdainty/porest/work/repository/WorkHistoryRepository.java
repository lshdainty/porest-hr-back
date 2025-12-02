package com.lshdainty.porest.work.repository;

import com.lshdainty.porest.work.domain.WorkHistory;
import com.lshdainty.porest.work.repository.dto.WorkHistorySearchCondition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface WorkHistoryRepository {
    // 신규 업무 이력 저장
    void save(WorkHistory workHistory);

    // 신규 업무 이력 다건 저장
    void saveAll(List<WorkHistory> workHistories);

    // 단건 업무 이력 조회
    Optional<WorkHistory> findById(Long id);

    // 전체 업무 이력 조회
    List<WorkHistory> findAll(WorkHistorySearchCondition condition);

    // 업무 이력 삭제 (Soft Delete)
    void delete(WorkHistory workHistory);

    // 전체 업무 이력 스트림 조회
    Stream<WorkHistory> findAllStream(WorkHistorySearchCondition condition);

    // 특정 사용자의 특정 날짜 업무 내역 목록 조회
    List<WorkHistory> findByUserAndDate(String userId, LocalDate date);

    // 특정 사용자의 기간 내 날짜별 업무 시간 합계 조회
    Map<LocalDate, BigDecimal> findDailyWorkHoursByUserAndPeriod(String userId, LocalDate startDate, LocalDate endDate);

    // 여러 사용자의 기간 내 (사용자ID, 날짜)별 업무 시간 합계 조회 (벌크 조회)
    Map<String, Map<LocalDate, BigDecimal>> findDailyWorkHoursByUsersAndPeriod(List<String> userIds, LocalDate startDate, LocalDate endDate);
}
