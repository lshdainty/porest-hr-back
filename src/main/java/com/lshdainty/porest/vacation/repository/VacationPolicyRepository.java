package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationPolicy;

import java.util.List;
import java.util.Optional;

public interface VacationPolicyRepository {
    // 신규 휴가 정책 저장
    void save(VacationPolicy vacationPolicy);

    // 단일 휴가 정책 조회
    Optional<VacationPolicy> findVacationPolicyById(Long vacationPolicyId);

    // 전체 휴가 정책 조회
    List<VacationPolicy> findVacationPolicies();

    // 휴가 정책명 중복 확인
    boolean existsByName(String name);
}
