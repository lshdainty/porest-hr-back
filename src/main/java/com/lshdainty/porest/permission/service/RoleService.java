package com.lshdainty.porest.permission.service;

import com.lshdainty.porest.common.exception.DuplicateException;
import com.lshdainty.porest.common.exception.EntityNotFoundException;
import com.lshdainty.porest.common.exception.ErrorCode;
import com.lshdainty.porest.permission.domain.Permission;
import com.lshdainty.porest.permission.domain.Role;
import com.lshdainty.porest.permission.repository.PermissionRepository;
import com.lshdainty.porest.permission.repository.RoleRepository;
import com.lshdainty.porest.permission.type.ActionType;
import com.lshdainty.porest.permission.type.ResourceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role & Permission Service<br>
 * 역할 및 권한 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    /* ==================== Role 관련 메서드 ==================== */

    /**
     * 전체 역할 목록 조회 (권한 포함)
     *
     * @return List<Role>
     */
    public List<Role> getAllRoles() {
        log.debug("전체 역할 목록 조회");
        return roleRepository.findAllRolesWithPermissions();
    }

    /**
     * 특정 역할 조회 (권한 포함)
     *
     * @param roleCode 역할 코드
     * @return Role
     */
    public Role getRole(String roleCode) {
        log.debug("역할 조회: roleCode={}", roleCode);
        return roleRepository.findByCodeWithPermissions(roleCode)
                .orElseThrow(() -> {
                    log.warn("역할 조회 실패 - 존재하지 않는 역할: roleCode={}", roleCode);
                    return new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND);
                });
    }

    /**
     * 역할 생성
     *
     * @param roleCode 역할 코드
     * @param roleName 역할 이름
     * @param desc 역할 설명
     * @return Role
     */
    @Transactional
    public Role createRole(String roleCode, String roleName, String desc) {
        log.debug("역할 생성 시작: roleCode={}, roleName={}", roleCode, roleName);
        if (roleRepository.findByCode(roleCode).isPresent()) {
            log.warn("역할 생성 실패 - 중복 코드: roleCode={}", roleCode);
            throw new DuplicateException(ErrorCode.ROLE_ALREADY_EXISTS);
        }
        Role role = Role.createRole(roleCode, roleName, desc);
        roleRepository.save(role);
        log.info("역할 생성 완료: roleCode={}", roleCode);
        return role;
    }

    /**
     * 역할 생성 (권한 포함)
     *
     * @param roleCode 역할 코드
     * @param roleName 역할 이름
     * @param desc 역할 설명
     * @param permissionCodes 권한 코드 리스트
     * @return Role
     */
    @Transactional
    public Role createRoleWithPermissions(String roleCode, String roleName, String desc, List<String> permissionCodes) {
        log.debug("역할 생성 (권한 포함) 시작: roleCode={}, roleName={}, permissionCount={}", roleCode, roleName, permissionCodes.size());
        if (roleRepository.findByCode(roleCode).isPresent()) {
            log.warn("역할 생성 실패 - 중복 코드: roleCode={}", roleCode);
            throw new DuplicateException(ErrorCode.ROLE_ALREADY_EXISTS);
        }

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> {
                            log.warn("역할 생성 실패 - 존재하지 않는 권한: permissionCode={}", code);
                            return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                        }))
                .collect(Collectors.toList());

        Role role = Role.createRoleWithPermissions(roleCode, roleName, desc, permissions);
        roleRepository.save(role);
        log.info("역할 생성 (권한 포함) 완료: roleCode={}, permissionCount={}", roleCode, permissions.size());
        return role;
    }

    /**
     * 역할 정보 수정 (설명만)
     *
     * @param roleCode 역할 코드
     * @param desc 역할 설명
     */
    @Transactional
    public void updateRole(String roleCode, String desc) {
        log.debug("역할 수정 시작: roleCode={}", roleCode);
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> {
                    log.warn("역할 수정 실패 - 존재하지 않는 역할: roleCode={}", roleCode);
                    return new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND);
                });
        role.updateRole(null, desc, null);
        log.info("역할 수정 완료: roleCode={}", roleCode);
    }

    /**
     * 역할 정보 수정 (설명 + 권한)
     *
     * @param roleCode 역할 코드
     * @param desc 역할 설명
     * @param permissionCodes 권한 코드 리스트
     */
    @Transactional
    public void updateRoleWithPermissions(String roleCode, String desc, List<String> permissionCodes) {
        log.debug("역할 수정 (권한 포함) 시작: roleCode={}", roleCode);
        Role role = getRole(roleCode);

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> {
                            log.warn("역할 수정 실패 - 존재하지 않는 권한: permissionCode={}", code);
                            return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                        }))
                .collect(Collectors.toList());

        role.updateRole(null, desc, permissions);
        log.info("역할 수정 (권한 포함) 완료: roleCode={}", roleCode);
    }

    /**
     * 역할의 권한만 수정
     *
     * @param roleCode 역할 코드
     * @param permissionCodes 권한 코드 리스트
     */
    @Transactional
    public void updateRolePermissions(String roleCode, List<String> permissionCodes) {
        log.debug("역할 권한 수정 시작: roleCode={}", roleCode);
        Role role = getRole(roleCode);

        List<Permission> permissions = permissionCodes.stream()
                .map(code -> permissionRepository.findByCode(code)
                        .orElseThrow(() -> {
                            log.warn("역할 권한 수정 실패 - 존재하지 않는 권한: permissionCode={}", code);
                            return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                        }))
                .collect(Collectors.toList());

        role.clearPermissions();
        permissions.forEach(role::addPermission);
        log.info("역할 권한 수정 완료: roleCode={}, permissionCount={}", roleCode, permissions.size());
    }

    /**
     * 역할에 권한 추가
     *
     * @param roleCode 역할 코드
     * @param permissionCode 권한 코드
     */
    @Transactional
    public void addPermissionToRole(String roleCode, String permissionCode) {
        log.debug("역할에 권한 추가 시작: roleCode={}, permissionCode={}", roleCode, permissionCode);
        Role role = getRole(roleCode);
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> {
                    log.warn("권한 추가 실패 - 존재하지 않는 권한: permissionCode={}", permissionCode);
                    return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        role.addPermission(permission);
        log.info("역할에 권한 추가 완료: roleCode={}, permissionCode={}", roleCode, permissionCode);
    }

    /**
     * 역할에서 권한 제거
     *
     * @param roleCode 역할 코드
     * @param permissionCode 권한 코드
     */
    @Transactional
    public void removePermissionFromRole(String roleCode, String permissionCode) {
        log.debug("역할에서 권한 제거 시작: roleCode={}, permissionCode={}", roleCode, permissionCode);
        Role role = getRole(roleCode);
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> {
                    log.warn("권한 제거 실패 - 존재하지 않는 권한: permissionCode={}", permissionCode);
                    return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        role.removePermission(permission);
        log.info("역할에서 권한 제거 완료: roleCode={}, permissionCode={}", roleCode, permissionCode);
    }

    /**
     * 역할 삭제 (Soft Delete)
     *
     * @param roleCode 역할 코드
     */
    @Transactional
    public void deleteRole(String roleCode) {
        log.debug("역할 삭제 시작: roleCode={}", roleCode);
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> {
                    log.warn("역할 삭제 실패 - 존재하지 않는 역할: roleCode={}", roleCode);
                    return new EntityNotFoundException(ErrorCode.ROLE_NOT_FOUND);
                });
        role.deleteRole();
        log.info("역할 삭제 완료: roleCode={}", roleCode);
    }

    /* ==================== Permission 관련 메서드 ==================== */

    /**
     * 전체 권한 목록 조회
     *
     * @return List<Permission>
     */
    public List<Permission> getAllPermissions() {
        log.debug("전체 권한 목록 조회");
        return permissionRepository.findAllPermissions();
    }

    /**
     * 특정 권한 조회
     *
     * @param permissionCode 권한 코드
     * @return Permission
     */
    public Permission getPermission(String permissionCode) {
        log.debug("권한 조회: permissionCode={}", permissionCode);
        return permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> {
                    log.warn("권한 조회 실패 - 존재하지 않는 권한: permissionCode={}", permissionCode);
                    return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                });
    }

    /**
     * 리소스별 권한 목록 조회
     *
     * @param resource 리소스명
     * @return List<Permission>
     */
    public List<Permission> getPermissionsByResource(String resource) {
        log.debug("리소스별 권한 목록 조회: resource={}", resource);
        return permissionRepository.findByResource(resource);
    }

    /**
     * 권한 생성
     *
     * @param code 권한 코드
     * @param name 권한 이름 (한글명)
     * @param desc 권한 설명
     * @param resource 리소스
     * @param action 액션
     * @return Permission
     */
    @Transactional
    public Permission createPermission(String code, String name, String desc, String resource, String action) {
        log.debug("권한 생성 시작: code={}, name={}", code, name);
        if (permissionRepository.findByCode(code).isPresent()) {
            log.warn("권한 생성 실패 - 중복 코드: code={}", code);
            throw new DuplicateException(ErrorCode.PERMISSION_ALREADY_EXISTS);
        }
        ResourceType resourceType = ResourceType.valueOf(resource);
        ActionType actionType = ActionType.valueOf(action);
        Permission permission = Permission.createPermission(code, name, desc, resourceType, actionType);
        permissionRepository.save(permission);
        log.info("권한 생성 완료: code={}", code);
        return permission;
    }

    /**
     * 권한 수정
     *
     * @param code 권한 코드
     * @param name 권한 이름 (한글명)
     * @param desc 권한 설명
     * @param resource 리소스
     * @param action 액션
     */
    @Transactional
    public void updatePermission(String code, String name, String desc, String resource, String action) {
        log.debug("권한 수정 시작: code={}", code);
        Permission permission = getPermission(code);
        ResourceType resourceType = resource != null ? ResourceType.valueOf(resource) : null;
        ActionType actionType = action != null ? ActionType.valueOf(action) : null;
        permission.updatePermission(name, desc, resourceType, actionType);
        log.info("권한 수정 완료: code={}", code);
    }

    /**
     * 권한 삭제 (Soft Delete)
     *
     * @param permissionCode 권한 코드
     */
    @Transactional
    public void deletePermission(String permissionCode) {
        log.debug("권한 삭제 시작: permissionCode={}", permissionCode);
        Permission permission = permissionRepository.findByCode(permissionCode)
                .orElseThrow(() -> {
                    log.warn("권한 삭제 실패 - 존재하지 않는 권한: permissionCode={}", permissionCode);
                    return new EntityNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
                });
        permission.deletePermission();
        log.info("권한 삭제 완료: permissionCode={}", permissionCode);
    }
}
