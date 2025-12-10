package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationUsage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VacationUsageRepository {
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

    /**
     * 유저 ID와 년도로 VacationUsage 조회
     * - startDate가 해당 년도에 포함되는 VacationUsage 조회
     *
     * @param userId 유저 ID
     * @param year 년도
     * @return 해당 년도의 VacationUsage 리스트
     */
    List<VacationUsage> findByUserIdAndYear(String userId, int year);

    /**
     * 모든 VacationUsage 조회 (User 정보 포함)
     * 모든 유저의 휴가 사용 내역을 조회
     *
     * @return 모든 VacationUsage 리스트
     */
    List<VacationUsage> findAllWithUser();

    /**
     * 기간별 VacationUsage 조회 (User 정보 포함)
     * startDate가 지정된 기간 사이에 있는 휴가 사용 내역 조회
     *
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 기간 내 VacationUsage 리스트
     */
    List<VacationUsage> findByPeriodWithUser(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 유저별 기간별 VacationUsage 조회 (User 정보 포함)
     * 특정 유저의 startDate가 지정된 기간 사이에 있는 휴가 사용 내역 조회
     *
     * @param userId 유저 ID
     * @param startDate 조회 시작일
     * @param endDate 조회 종료일
     * @return 기간 내 VacationUsage 리스트
     */
    List<VacationUsage> findByUserIdAndPeriodWithUser(String userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * baseTime 이전에 사용한 VacationUsage 조회
     * startDate <= baseTime인 휴가 사용 내역 조회
     *
     * @param userId 유저 ID
     * @param baseTime 기준 시간
     * @return baseTime 이전 VacationUsage 리스트
     */
    List<VacationUsage> findUsedByUserIdAndBaseTime(String userId, LocalDateTime baseTime);

    /**
     * baseTime 이후 사용 예정인 VacationUsage 조회
     * startDate > baseTime인 휴가 사용 내역 조회
     *
     * @param userId 유저 ID
     * @param baseTime 기준 시간
     * @return baseTime 이후 VacationUsage 리스트
     */
    List<VacationUsage> findExpectedByUserIdAndBaseTime(String userId, LocalDateTime baseTime);

    /**
     * 특정 사용자의 특정 기간 내 VacationUsage 조회
     * startDate가 startOfPeriod와 endOfPeriod 사이에 있는 휴가 사용 내역 조회
     *
     * @param userId 사용자 ID
     * @param startOfPeriod 조회 기간 시작일
     * @param endOfPeriod 조회 기간 종료일
     * @return 해당 기간 내 VacationUsage 리스트
     */
    List<VacationUsage> findByUserIdAndPeriod(String userId, LocalDateTime startOfPeriod, LocalDateTime endOfPeriod);

    /**
     * 특정 사용자의 기간 내 VacationUsage 조회 (일별 집계용)
     *
     * @param userId 사용자 ID
     * @param startDateTime 조회 기간 시작일시
     * @param endDateTime 조회 기간 종료일시
     * @return VacationUsage 리스트
     */
    List<VacationUsage> findByUserIdAndPeriodForDaily(String userId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * 여러 사용자의 기간 내 VacationUsage 조회 (일별 집계용, 벌크 조회)
     *
     * @param userIds 사용자 ID 리스트
     * @param startDateTime 조회 기간 시작일시
     * @param endDateTime 조회 기간 종료일시
     * @return VacationUsage 리스트
     */
    List<VacationUsage> findByUserIdsAndPeriodForDaily(List<String> userIds, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
