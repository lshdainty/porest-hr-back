package com.porest.hr.common.repository;

import com.porest.hr.common.domain.SystemCode;

import java.util.List;
import java.util.Optional;

/**
 * 시스템 타입 코드 Repository
 */
public interface SystemCodeRepository {
    /**
     * 활성 상태의 모든 시스템 타입 조회 (정렬 순서)
     */
    List<SystemCode> findAllActive();

    /**
     * 코드로 시스템 타입 조회
     */
    Optional<SystemCode> findByCode(String code);
}
