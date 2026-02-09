package com.porest.hr.common.repository;

import com.porest.hr.common.domain.CompanyCode;

import java.util.List;
import java.util.Optional;

/**
 * 회사 타입 코드 Repository
 */
public interface CompanyCodeRepository {
    /**
     * 활성 상태의 모든 회사 타입 조회 (정렬 순서)
     */
    List<CompanyCode> findAllActive();

    /**
     * 코드로 회사 타입 조회
     */
    Optional<CompanyCode> findByCode(String code);
}
