package com.porest.hr.permission.repository;

import com.porest.hr.permission.domain.Role;

import java.util.List;
import java.util.Optional;

/**
 * Role Repository Interface<br>
 * QueryDSL을 활용한 역할 조회 인터페이스
 */
public interface RoleRepository {
    /**
     * 신규 역할 저장
     *
     * @param role 저장할 역할
     */
    void save(Role role);

    /**
     * 역할 코드로 역할 조회
     *
     * @param code 역할 코드
     * @return Optional<Role>
     */
    Optional<Role> findByCode(String code);

    /**
     * 역할 코드로 역할 조회 (권한 포함 - fetch join)
     *
     * @param code 역할 코드
     * @return Optional<Role>
     */
    Optional<Role> findByCodeWithPermissions(String code);

    /**
     * 전체 역할 목록 조회 (권한 포함 - fetch join)
     *
     * @return List<Role>
     */
    List<Role> findAllRolesWithPermissions();
}
