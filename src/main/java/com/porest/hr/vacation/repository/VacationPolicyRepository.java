package com.porest.hr.vacation.repository;

import com.porest.hr.vacation.domain.VacationPolicy;
import com.porest.hr.vacation.type.VacationType;

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
     * @param rowId 휴가 정책 rowId
     * @return Optional&lt;VacationPolicy&gt;
     */
    Optional<VacationPolicy> findByRowId(Long rowId);

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

    /**
     * 휴가 타입으로 휴가 정책 목록 조회
     *
     * @param vacationType 휴가 타입
     * @return List&lt;VacationPolicy&gt;
     */
    List<VacationPolicy> findByVacationType(VacationType vacationType);
}
