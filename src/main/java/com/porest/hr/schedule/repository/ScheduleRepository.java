package com.porest.hr.schedule.repository;

import com.porest.hr.schedule.domain.Schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Schedule Repository Interface
 */
public interface ScheduleRepository {
    /**
     * 신규 스케줄 저장
     *
     * @param schedule 저장할 스케줄
     */
    void save(Schedule schedule);

    /**
     * 스케줄 단건 조회
     *
     * @param scheduleId 스케줄 ID
     * @return Optional&lt;Schedule&gt;
     */
    Optional<Schedule> findById(Long scheduleId);

    /**
     * 유저 스케줄 조회
     *
     * @param userId 유저 ID
     * @return List&lt;Schedule&gt;
     */
    List<Schedule> findSchedulesByUserId(String userId);

    /**
     * 기간에 해당하는 스케줄 조회
     *
     * @param start 조회 시작일시
     * @param end 조회 종료일시
     * @return List&lt;Schedule&gt;
     */
    List<Schedule> findSchedulesByPeriod(LocalDateTime start, LocalDateTime end);
}
