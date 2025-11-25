package com.lshdainty.porest.permission.repository;

import com.lshdainty.porest.permission.domain.Role;

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
     * 역할 ID로 역할 조회
     *
     * @param id 역할 ID
     * @return Optional<Role>
     */
    Optional<Role> findById(String id);

    /**
     * 역할 ID로 역할 조회 (권한 포함 - fetch join)
     *
     * @param id 역할 ID
     * @return Optional<Role>
     */
    Optional<Role> findByIdWithPermissions(String id);

    /**
     * 전체 역할 목록 조회 (삭제되지 않은 것만)
     *
     * @return List<Role>
     */
    List<Role> findAllRoles();

    /**
     * 전체 역할 목록 조회 (권한 포함 - fetch join)
     *
     * @return List<Role>
     */
    List<Role> findAllRolesWithPermissions();
}
