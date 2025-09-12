package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Schedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository {
    // 신규 스케쥴 저장
    void save(Schedule schedule);
    // 스케줄 단건 조회(delete용)
    Optional<Schedule> findById(Long scheduleId);
    // 유저 스케줄 조회
    List<Schedule> findSchedulesByUserId(String userId);
    // 기간에 해당하는 스케줄 조회
    List<Schedule> findSchedulesByPeriod(LocalDateTime start, LocalDateTime end);
}
