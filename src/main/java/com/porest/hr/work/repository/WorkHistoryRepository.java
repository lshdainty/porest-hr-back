package com.porest.hr.work.repository;

import com.porest.hr.work.domain.WorkHistory;
import com.porest.hr.work.repository.dto.WorkHistorySearchCondition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * WorkHistory Repository Interface
 */
public interface WorkHistoryRepository {
    /**
     * 신규 업무 이력 저장
     *
     * @param workHistory 저장할 업무 이력
     */
    void save(WorkHistory workHistory);

    /**
     * 신규 업무 이력 다건 저장
     *
     * @param workHistories 저장할 업무 이력 리스트
     */
    void saveAll(List<WorkHistory> workHistories);

    /**
     * 단건 업무 이력 조회
     *
     * @param id 업무 이력 ID
     * @return Optional&lt;WorkHistory&gt;
     */
    Optional<WorkHistory> findById(Long id);

    /**
     * 전체 업무 이력 조회
     *
     * @param condition 검색 조건
     * @return List&lt;WorkHistory&gt;
     */
    List<WorkHistory> findAll(WorkHistorySearchCondition condition);

    /**
     * 업무 이력 삭제 (Soft Delete)
     *
     * @param workHistory 삭제할 업무 이력
     */
    void delete(WorkHistory workHistory);

    /**
     * 전체 업무 이력 스트림 조회
     *
     * @param condition 검색 조건
     * @return Stream&lt;WorkHistory&gt;
     */
    Stream<WorkHistory> findAllStream(WorkHistorySearchCondition condition);

    /**
     * 특정 사용자의 특정 날짜 업무 내역 목록 조회
     *
     * @param userId 사용자 ID
     * @param date 조회 날짜
     * @return List&lt;WorkHistory&gt;
     */
    List<WorkHistory> findByUserAndDate(String userId, LocalDate date);

    /**
     * 특정 사용자의 기간 내 날짜별 업무 시간 합계 조회
     *
     * @param userId 사용자 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return Map&lt;LocalDate, BigDecimal&gt;
     */
    Map<LocalDate, BigDecimal> findDailyWorkHoursByUserAndPeriod(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * 여러 사용자의 기간 내 (사용자ID, 날짜)별 업무 시간 합계 조회 (벌크 조회)
     *
     * @param userIds 사용자 ID 리스트
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return Map&lt;String, Map&lt;LocalDate, BigDecimal&gt;&gt;
     */
    Map<String, Map<LocalDate, BigDecimal>> findDailyWorkHoursByUsersAndPeriod(List<String> userIds, LocalDate startDate, LocalDate endDate);
}
