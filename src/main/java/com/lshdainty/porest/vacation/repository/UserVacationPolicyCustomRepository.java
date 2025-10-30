package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.UserVacationPolicy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserVacationPolicyCustomRepository {
    // 유저 휴가 정책 저장
    void save(UserVacationPolicy userVacationPolicy);

    // 유저 휴가 정책 일괄 저장
    void saveAll(List<UserVacationPolicy> userVacationPolicies);

    // 유저 ID로 휴가 정책 조회
    List<UserVacationPolicy> findByUserId(String userId);

    // 유저 ID와 휴가 정책 ID 조합이 이미 존재하는지 확인
    boolean existsByUserIdAndVacationPolicyId(String userId, Long vacationPolicyId);

    // UserVacationPolicy ID로 단일 조회
    Optional<UserVacationPolicy> findById(Long userVacationPolicyId);

    // 유저 ID와 휴가 정책 ID로 조회
    Optional<UserVacationPolicy> findByUserIdAndVacationPolicyId(String userId, Long vacationPolicyId);

    // 휴가 정책 ID로 모든 UserVacationPolicy 조회
    List<UserVacationPolicy> findByVacationPolicyId(Long vacationPolicyId);

    // 오늘 부여 대상인 반복 부여 정책 조회 (스케줄러용)
    List<UserVacationPolicy> findRepeatGrantTargetsForToday(LocalDate today);
}
