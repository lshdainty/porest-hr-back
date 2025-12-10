package com.lshdainty.porest.vacation.repository;

import com.lshdainty.porest.vacation.domain.VacationPolicy;

import java.util.List;
import java.util.Optional;

/**
 * VacationPolicy Repository Interface
 */
public interface VacationPolicyRepository {
    /**
     * 신규 휴가 정책 저장
     *
     * @param vacationPolicy 저장할 휴가 정책
     */
    void save(VacationPolicy vacationPolicy);

    /**
     * 단일 휴가 정책 조회
     *
     * @param vacationPolicyId 휴가 정책 ID
     * @return Optional&lt;VacationPolicy&gt;
     */
    Optional<VacationPolicy> findVacationPolicyById(Long vacationPolicyId);

    /**
     * 전체 휴가 정책 조회
     *
     * @return List&lt;VacationPolicy&gt;
     */
    List<VacationPolicy> findVacationPolicies();

    /**
     * 휴가 정책명 중복 확인
     *
     * @param name 휴가 정책명
     * @return 중복 여부
     */
    boolean existsByName(String name);
}
