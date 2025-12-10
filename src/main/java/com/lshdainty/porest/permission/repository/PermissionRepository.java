package com.lshdainty.porest.permission.repository;

import com.lshdainty.porest.permission.domain.Permission;

import java.util.List;
import java.util.Optional;

/**
 * Permission Repository Interface<br>
 * QueryDSL을 활용한 권한 조회 인터페이스
 */
public interface PermissionRepository {
    /**
     * 신규 권한 저장
     *
     * @param permission 저장할 권한
     */
    void save(Permission permission);

    /**
     * 권한 ID로 권한 조회
     *
     * @param id 권한 ID
     * @return Optional<Permission>
     */
    Optional<Permission> findById(Long id);

    /**
     * 권한 코드로 권한 조회
     *
     * @param code 권한 코드
     * @return Optional<Permission>
     */
    Optional<Permission> findByCode(String code);

    /**
     * 전체 권한 목록 조회 (삭제되지 않은 것만)
     *
     * @return List<Permission>
     */
    List<Permission> findAllPermissions();

    /**
     * 리소스별 권한 목록 조회
     *
     * @param resource 리소스명
     * @return List<Permission>
     */
    List<Permission> findByResource(String resource);

    /**
     * 리소스와 액션으로 권한 조회
     *
     * @param resource 리소스명
     * @param action 액션명
     * @return Optional<Permission>
     */
    Optional<Permission> findByResourceAndAction(String resource, String action);
}
