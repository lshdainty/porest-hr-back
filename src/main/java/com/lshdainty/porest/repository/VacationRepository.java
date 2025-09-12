package com.lshdainty.porest.repository;

import com.lshdainty.porest.domain.Vacation;
import com.lshdainty.porest.type.VacationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VacationRepository {
    // 휴가 저장
    void save(Vacation vacation);
    // 단건 휴가 조회(delete용)
    Optional<Vacation> findById(Long vacationId);
    // 유저에 부여된 전체 휴가 조회
    List<Vacation> findVacationsByUserId(String userId);
    // 연차, OT 신규 등록이 필요한지 조회
    Optional<Vacation> findVacationByTypeWithYear(String userId, VacationType type, String year);
    // 유저가 가진 휴가 중 기준 시간을 포함하는 휴가 리스트 조회
    List<Vacation> findVacationsByBaseTime(String userId, LocalDateTime baseTime);
    // 유저가 가진 휴가 중 기준 시간을 포함하는 휴가와 스케줄 조회
    List<Vacation> findVacationsByBaseTimeWithHistory(String userId, LocalDateTime baseTime);
    // 휴가 여러건 조회(IDS, 유저정보 포함)
    List<Vacation> findVacationsByIdsWithUser(List<Long> vacationIds);
}
