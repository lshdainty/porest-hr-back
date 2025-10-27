package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.UserVacationPolicy;

import java.util.List;

public interface UserVacationPolicyCustomRepository {
    // 유저 휴가 정책 저장
    void save(UserVacationPolicy userVacationPolicy);

    // 유저 휴가 정책 일괄 저장
    void saveAll(List<UserVacationPolicy> userVacationPolicies);

    // 유저 ID로 휴가 정책 조회
    List<UserVacationPolicy> findByUserId(String userId);

    // 유저 ID와 휴가 정책 ID 조합이 이미 존재하는지 확인
    boolean existsByUserIdAndVacationPolicyId(String userId, Long vacationPolicyId);
}
